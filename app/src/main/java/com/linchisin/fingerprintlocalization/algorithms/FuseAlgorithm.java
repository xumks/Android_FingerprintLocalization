package com.linchisin.fingerprintlocalization.algorithms;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author LinChiSin
 * @date 2018-5-7 下午7:49:23
 * @description PDR融合算法
 */

public class FuseAlgorithm {

    private static final double RP_DISTANCE=1.5;


    public Position fuseWifiWithPdr(Position currentPosition,float radioMap[][], int index[],int wifiK, float stepLength, int orient,int fuseK){
        //计算上一个距离到wifi各RP的距离及方向
        double []stepDistance=new double[wifiK];
        double []angleDistance=new double[wifiK];
        double []distance=new double[wifiK];
        int[] indexK=new int[wifiK];
        System.arraycopy(index, 0, indexK, 0, wifiK);
        for (int i = 0; i < wifiK; i++) {
            double deltaX=radioMap[index[i]][0]-currentPosition.x;
            double deltaY=radioMap[index[i]][1]-currentPosition.y;
            stepDistance[i]=Math.abs(Math.sqrt((Math.pow(deltaX,2)+Math.pow(deltaY,2)))-stepLength);
            if(deltaX!=0){
                angleDistance[i]=RP_DISTANCE*Math.sin(Math.abs((int)Math.toDegrees(Math.atan(deltaY/deltaX))-(90-orient)));
            }else if(deltaY>0){
                angleDistance[i]=RP_DISTANCE*Math.sin(Math.abs(90-(90-orient)));
            }else if(deltaY<0){
                angleDistance[i]=RP_DISTANCE*Math.sin(Math.abs(270-(90-orient)));
            }else{
                angleDistance[i]=RP_DISTANCE*Math.sin(Math.abs(0-(90-orient)));
            }
            distance[i]=stepDistance[i]+angleDistance[i];
        }
        //将距离与序号进行关联，使得对距离进行排序时，序号也能对应排序
        LinkedHashMap<Integer,Double> linkedHashMap=new LinkedHashMap<>();
        for (int i = 0; i < wifiK; i++) {
            linkedHashMap.put(indexK[i],distance[i]);
        }
        List<Map.Entry<Integer,Double>> linkedList=new LinkedList<>(linkedHashMap.entrySet());
        Collections.sort(linkedList, new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        //获取按距离排序后的序号
        int []indexFuseK=new int[wifiK];
        for (int i = 0; i < wifiK; i++) {
            indexFuseK[i]=linkedList.get(i).getKey();
        }
        // 计算位置
        float x = 0;
        float y = 0;
        float z = 0;

        for (int i = 0; i < fuseK; i++) {
            x += radioMap[indexFuseK[i]][0];  // radioMap 的长度一定是N+2的，多一个x, 一个y, 一个z
            y += radioMap[indexFuseK[i]][1];
            z += radioMap[indexFuseK[i]][2];
        }

        x = x / fuseK;
        y = y / fuseK;
        z = z / fuseK;

        return new Position(x,y,z);

    }
}
