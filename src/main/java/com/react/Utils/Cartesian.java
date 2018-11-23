package com.react.Utils;

import java.util.ArrayList;
import java.util.List;
import com.react.verify.OneDimensionalEc;

public class Cartesian {
    public static void recursive (List<List<OneDimensionalEc>> dimValue, List<List<OneDimensionalEc>> result, int layer, List<OneDimensionalEc> curList) {
        if (layer < dimValue.size() - 1) {
            if (dimValue.get(layer).size() == 0) {
                recursive(dimValue, result, layer + 1, curList);
            } else {
                for (int i = 0; i < dimValue.get(layer).size(); i++) {
                    List<OneDimensionalEc> list = new ArrayList<OneDimensionalEc>(curList);
                    list.add(dimValue.get(layer).get(i));
                    recursive(dimValue, result, layer + 1, list);
                }
            }
        } else if (layer == dimValue.size() - 1) {
            if (dimValue.get(layer).size() == 0) {
                result.add(curList);
            } else {
                for (int i = 0; i < dimValue.get(layer).size(); i++) {
                    List<OneDimensionalEc> list = new ArrayList<OneDimensionalEc>(curList);
                    list.add(dimValue.get(layer).get(i));
                    result.add(list);
                }
            }
        }
    }


}
