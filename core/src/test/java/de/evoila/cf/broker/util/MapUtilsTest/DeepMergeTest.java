package de.evoila.cf.broker.util.MapUtilsTest;

import de.evoila.cf.broker.util.MapUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

class DeepMergeTest {

    private Map<String, Object> destinationMap;
    private Map<String, Object> originalDestinationMap; // immutable
    private Map<String, Object> sourceMap;  // initialized with Map.of, so it is immutable and we don't need an explicit check that it is not changed by the deepMerge method

    class ClassForTesting
    {
        Integer number;
        String text;
        Boolean flag;

        ClassForTesting(Integer number, String text, Boolean flag)
        {
            this.number = number;
            this.text = text;
            this.flag = flag;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClassForTesting that = (ClassForTesting) o;
            return Objects.equals(number, that.number) &&
                    Objects.equals(text, that.text) &&
                    Objects.equals(flag, that.flag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(number, text, flag);
        }
    }

    @BeforeEach
    void setUp()
    {
        destinationMap = new HashMap<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("firstSubKey", new ClassForTesting(0, "a", true) );
        map1.put("secondSubKey", new ClassForTesting(1, "bc", false) );
        map1.put("thirdSubKey", new ClassForTesting(2, "de", true) );
        destinationMap.put("firstKey", map1);
        destinationMap.put("secondKey", new HashMap<>() );
        Map<String, Object> map3 = new HashMap<>();
        map3.put("firstSubKey", new ClassForTesting(23, "def", true) );
        map3.put("secondSubKey", new ClassForTesting(45, "ghi", false) );
        map3.put("thirdSubKey" , new ClassForTesting(67, "jkl" ,false) );
        destinationMap.put("thirdKey", map3);
        destinationMap.put("forthKey", "StringValue");

        originalDestinationMap = Map.of("firstKey", Map.of("firstSubKey", new ClassForTesting(0, "a", true),
                                                                "secondSubKey", new ClassForTesting(1, "bc", false),
                                                                "thirdSubKey", new ClassForTesting(2, "de", true) ),
                                        "secondKey", Collections.EMPTY_MAP,
                                        "thirdKey", Map.of("firstSubKey", new ClassForTesting(23, "def", true),
                                                            "secondSubKey", new ClassForTesting(45, "ghi", false),
                                                            "thirdSubKey" , new ClassForTesting(67, "jkl" ,false) ),
                                        "forthKey", "StringValue");

        sourceMap = Map.of("firstKey", Map.of("firstSubKey", new ClassForTesting(0, "z", true),
                                                    "secondSubKey", new ClassForTesting(4, "xyz", true),
                                                    "forthSubKey", new ClassForTesting(7, "rstu", false)),
                            "secondKey", Map.of("fourthSubKey", new ClassForTesting(100, "a", true),
                                                    "fifthSubKey", new ClassForTesting(101, "klm", false),
                                                    "thirdSubKey", new ClassForTesting(102, "opqrs", true)),
                            "thirdKey", Collections.EMPTY_MAP,
                            "forthKey", Map.of("firstSubKey", new ClassForTesting(32, "mnop", false) ),
                            "fifthKey", Map.of("sixthSubKey", new ClassForTesting(43, "ponm", true),
                                                    "secondSubKey", new ClassForTesting(1, "b", true) ) );
    }

    @Test
    void destinationMapNull()
    {
        MapUtils.deepMerge(null, sourceMap);
    }

    @Test
    void sourceMapNull() // destination map shall remain unchanged
    {
        MapUtils.deepMerge(destinationMap, null);
        assertEquals(originalDestinationMap, destinationMap);
    }

    @Test
    void bothMapsEmpty() // destination map remains empty
    {
        Map<String, Object> emptyDestination = new HashMap<>();
        Map<String, Object> emptySource = Collections.EMPTY_MAP;
        MapUtils.deepMerge(emptyDestination, emptySource);

        assertEquals(0, emptyDestination.size());
    }

    @Test
    void sourceMapEmpty() // destination map remains empty
    {
        Map<String, Object> emptySource = Collections.EMPTY_MAP;
        MapUtils.deepMerge(destinationMap, emptySource);
        assertEquals(originalDestinationMap, destinationMap);
    }

    @Test
    void destinationMapEmpty() // destination map will be a (shallow) copy of source map
    {
        Map<String, Object> emptyDestination = new HashMap<>();
        MapUtils.deepMerge(emptyDestination, sourceMap);
        assertEquals(sourceMap, emptyDestination);
    }

    @Test
    void mapsIdentical() // destination map unchanged
    {
        MapUtils.deepMerge(destinationMap, originalDestinationMap);
        assertEquals(originalDestinationMap, destinationMap);
    }

    @Test
    void bothMapsContainNoCollectionsAsValues()
    {
        Map<String, Object> simpleDestinationMap = new HashMap<>();
        simpleDestinationMap.put("firstKey", "abc");
        simpleDestinationMap.put("secondKey", 4);
        Map<String, Object> simpleSourceMap = Map.of("firstKey", 42,
                                                    "thirdKey", "xyz");
        MapUtils.deepMerge(simpleDestinationMap, simpleSourceMap);
        Map<String, Object> expectedDestinationMap = Map.of("firstKey", 42,
                                                            "secondKey", 4,
                                                            "thirdKey", "xyz");
        assertEquals(expectedDestinationMap, simpleDestinationMap);
    }

    @Test
    void bothMapsContainListsAsValues() // note that lists for identical keys are not merged, but the list in the destination map is overwritten
    {
        Map<String, Object> destinationMapWithList = new HashMap<>();
        destinationMapWithList.put("firstKey", new ArrayList<>(Arrays.asList(38, 50, 99) ) );
        destinationMapWithList.put("secondKey", new ArrayList<>(Arrays.asList("abc", "def", "ghi") ) );
        destinationMapWithList.put("thirdKey", new ArrayList<>(Arrays.asList(1, 2, 3) ) );
        destinationMapWithList.put("forthKey", "xyz");
        Map<String, Object> sourceMapWithList = Map.of("firstKey", List.of(17, 18, 19),
                                                        "secondKey", 44,
                                                        "forthKey", List.of(1, 2, 3),
                                                        "fifthKey", List.of("x", "y", "z"));
        MapUtils.deepMerge(destinationMapWithList, sourceMapWithList);

        Map<String, Object> expectedList = Map.of("firstKey", List.of(17, 18, 19),
                                                    "secondKey", 44,
                                                    "thirdKey", List.of(1, 2, 3),
                                                    "forthKey", List.of(1, 2, 3),
                                                    "fifthKey", List.of("x", "y", "z"));
        assertEquals(expectedList, destinationMapWithList);
    }

    @Test
    void bothMapsContainMapsAsValues()
    {
        MapUtils.deepMerge(destinationMap, sourceMap);

        Map<String, Object> expectedMap = Map.of("firstKey", Map.of("firstSubKey", new ClassForTesting(0, "z", true),
                                                                        "secondSubKey", new ClassForTesting(4, "xyz", true),
                                                                        "thirdSubKey", new ClassForTesting(2, "de", true),
                                                                        "forthSubKey", new ClassForTesting(7, "rstu", false) ),
                                                "secondKey", Map.of("fourthSubKey", new ClassForTesting(100, "a", true),
                                                                        "fifthSubKey", new ClassForTesting(101, "klm", false),
                                                                        "thirdSubKey", new ClassForTesting(102, "opqrs", true)),
                                                "thirdKey", Map.of("firstSubKey", new ClassForTesting(23, "def", true),
                                                                        "secondSubKey", new ClassForTesting(45, "ghi", false),
                                                                        "thirdSubKey" , new ClassForTesting(67, "jkl" ,false) ),
                                                "forthKey", Map.of("firstSubKey", new ClassForTesting(32, "mnop", false) ),
                                                "fifthKey", Map.of("sixthSubKey", new ClassForTesting(43, "ponm", true),
                                                                        "secondSubKey", new ClassForTesting(1, "b", true) ));
        assertEquals(expectedMap, destinationMap);
    }
}