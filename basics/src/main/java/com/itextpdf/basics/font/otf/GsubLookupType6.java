package com.itextpdf.basics.font.otf;

import com.itextpdf.basics.font.otf.lookuptype6.SubTableLookup6Format1;
import com.itextpdf.basics.font.otf.lookuptype6.SubTableLookup6Format2;
import com.itextpdf.basics.font.otf.lookuptype6.SubTableLookup6Format3;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * LookupType 6: Chaining Contextual Substitution Subtable
 */
public class GsubLookupType6 extends GsubLookupType5 {
    protected GsubLookupType6(OpenTypeFontTableReader openReader, int lookupFlag, int[] subTableLocations) throws IOException {
        super(openReader, lookupFlag, subTableLocations);
    }

    @Override
    protected void readSubTableFormat1(int subTableLocation) throws IOException {
        HashMap<Integer, List<ContextualSubstRule>> substMap = new HashMap<>();

        int coverageOffset = openReader.rf.readUnsignedShort();
        int chainSubRuleSetCount = openReader.rf.readUnsignedShort();
        int[] chainSubRuleSetOffsets = openReader.readUShortArray(chainSubRuleSetCount, subTableLocation);

        List<Integer> coverageGlyphIds = openReader.readCoverageFormat(subTableLocation + coverageOffset);
        for (int i = 0; i < chainSubRuleSetCount; ++i) {
            openReader.rf.seek(chainSubRuleSetOffsets[i]);
            int chainSubRuleCount = openReader.rf.readUnsignedShort();
            int[] chainSubRuleOffsets = openReader.readUShortArray(chainSubRuleCount, chainSubRuleSetOffsets[i]);

            List<ContextualSubstRule> chainSubRuleSet = new ArrayList<>(chainSubRuleCount);
            for (int j = 0; j < chainSubRuleCount; ++j) {
                openReader.rf.seek(chainSubRuleOffsets[j]);
                int backtrackGlyphCount = openReader.rf.readUnsignedShort();
                int[] backtrackGlyphIds = openReader.readUShortArray(backtrackGlyphCount);
                int inputGlyphCount = openReader.rf.readUnsignedShort();
                int[] inputGlyphIds = openReader.readUShortArray(inputGlyphCount - 1);
                int lookAheadGlyphCount = openReader.rf.readUnsignedShort();
                int[] lookAheadGlyphIds = openReader.readUShortArray(lookAheadGlyphCount);
                int substCount = openReader.rf.readUnsignedShort();
                SubstLookupRecord[] substLookupRecords = openReader.readSubstLookupRecords(substCount);

                chainSubRuleSet.add(new SubTableLookup6Format1.SubstRuleFormat1(backtrackGlyphIds, inputGlyphIds, lookAheadGlyphIds, substLookupRecords));
            }
            substMap.put(coverageGlyphIds.get(i), chainSubRuleSet);
        }

        subTables.add(new SubTableLookup6Format1(openReader, lookupFlag, substMap));
    }

    @Override
    protected void readSubTableFormat2(int subTableLocation) throws IOException {
        int coverageOffset = openReader.rf.readUnsignedShort();
        int backtrackClassDefOffset = openReader.rf.readUnsignedShort();
        int inputClassDefOffset = openReader.rf.readUnsignedShort();
        int lookaheadClassDefOffset = openReader.rf.readUnsignedShort();
        int chainSubClassSetCount = openReader.rf.readUnsignedShort();
        int[] chainSubClassSetOffsets = openReader.readUShortArray(chainSubClassSetCount, subTableLocation);

        HashSet<Integer> coverageGlyphIds = new HashSet<>(openReader.readCoverageFormat(subTableLocation + coverageOffset));
        OtfClass backtrackClassDefinition = openReader.readClassDefinition(subTableLocation + backtrackClassDefOffset);
        OtfClass inputClassDefinition = openReader.readClassDefinition(subTableLocation + inputClassDefOffset);
        OtfClass lookaheadClassDefinition = openReader.readClassDefinition(subTableLocation + lookaheadClassDefOffset);

        SubTableLookup6Format2 t = new SubTableLookup6Format2(openReader, lookupFlag, coverageGlyphIds,
                backtrackClassDefinition, inputClassDefinition, lookaheadClassDefinition);

        List<List<ContextualSubstRule>> subClassSets = new ArrayList<>(chainSubClassSetCount);
        for (int i = 0; i < chainSubClassSetCount; ++i) {
            List<ContextualSubstRule> subClassSet = null;
            if (chainSubClassSetOffsets[i] != 0) {
                openReader.rf.seek(chainSubClassSetOffsets[i]);
                int chainSubClassRuleCount = openReader.rf.readUnsignedShort();
                int[] chainSubClassRuleOffsets = openReader.readUShortArray(chainSubClassRuleCount, chainSubClassSetOffsets[i]);

                subClassSet = new ArrayList<>(chainSubClassRuleCount);
                for (int j = 0; j < chainSubClassRuleCount; ++j) {
                    SubTableLookup6Format2.SubstRuleFormat2 rule;
                    openReader.rf.seek(chainSubClassRuleOffsets[j]);

                    int backtrackClassCount = openReader.rf.readUnsignedShort();
                    int[] backtrackClassIds = openReader.readUShortArray(backtrackClassCount);
                    int inputClassCount = openReader.rf.readUnsignedShort();
                    int[] inputClassIds = openReader.readUShortArray(inputClassCount - 1);
                    int lookAheadClassCount = openReader.rf.readUnsignedShort();
                    int[] lookAheadClassIds = openReader.readUShortArray(lookAheadClassCount);
                    int substCount = openReader.rf.readUnsignedShort();
                    SubstLookupRecord[] substLookupRecords = openReader.readSubstLookupRecords(substCount);

                    rule = t.new SubstRuleFormat2(backtrackClassIds, inputClassIds, lookAheadClassIds, substLookupRecords);
                    subClassSet.add(rule);
                }
            }
            subClassSets.add(subClassSet);
        }

        t.setSubClassSets(subClassSets);
        subTables.add(t);
    }

    @Override
    protected void readSubTableFormat3(int subTableLocation) throws IOException {
        int backtrackGlyphCount = openReader.rf.readUnsignedShort();
        int[] backtrackCoverageOffsets = openReader.readUShortArray(backtrackGlyphCount, subTableLocation);
        int inputGlyphCount = openReader.rf.readUnsignedShort();
        int[] inputCoverageOffsets = openReader.readUShortArray(inputGlyphCount, subTableLocation);
        int lookaheadGlyphCount = openReader.rf.readUnsignedShort();
        int[] lookaheadCoverageOffsets = openReader.readUShortArray(lookaheadGlyphCount, subTableLocation);
        int substCount = openReader.rf.readUnsignedShort();
        SubstLookupRecord[] substLookupRecords = openReader.readSubstLookupRecords(substCount);

        List<HashSet<Integer>> backtrackCoverages = new ArrayList<>(backtrackGlyphCount);
        openReader.readCoverages(backtrackCoverageOffsets, backtrackCoverages);

        List<HashSet<Integer>> inputCoverages = new ArrayList<>(inputGlyphCount);
        openReader.readCoverages(inputCoverageOffsets, inputCoverages);

        List<HashSet<Integer>> lookaheadCoverages = new ArrayList<>(lookaheadGlyphCount);
        openReader.readCoverages(lookaheadCoverageOffsets, lookaheadCoverages);

        SubTableLookup6Format3.SubstRuleFormat3 rule =
                new SubTableLookup6Format3.SubstRuleFormat3(backtrackCoverages, inputCoverages, lookaheadCoverages, substLookupRecords);
        subTables.add(new SubTableLookup6Format3(openReader, lookupFlag, rule));
    }
}
