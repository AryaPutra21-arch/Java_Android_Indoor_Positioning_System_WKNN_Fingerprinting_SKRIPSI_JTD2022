package com.talentica.wifiindoorpositioning.wifiindoorpositioning.model;

import java.util.HashMap;

public class RoomData {
    private static String[] lantai1,lantai2,lantai3;
    private static HashMap<Integer, Float[]> room = new HashMap<Integer, Float[]>();



    protected void Oncreate(){

    }

    public static String[] Lantai1(){
        lantai1 = new String[]{"Pintu Keluar lt_1","Lobi","Tangga","R. Dosen","Lab. Fiber Optik" };
        return lantai1;
    }

    public static String[] Lantai2(){
        lantai2 = new String[]{"Lobi","Tangga","Auditorium 1 AH","Auditorium AH 2" };

        return lantai2;
    }
    public static String[] Lantai3(){
        lantai3 = new String[]{"Tangga","Kelas 1","Kelas 2" };

        return lantai3;
    }
    public static float[] Get_Dest(){
        //room.put("Lobi", Float[]{650,700});
        return (new float[]{650,700});
    }
}
