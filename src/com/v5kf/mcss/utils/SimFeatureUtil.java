package com.v5kf.mcss.utils;

/**
 * 字符串相似度计算
 * @author V5KF_MBP
 *
 */
public class SimFeatureUtil {
	 
    private static int min(int one, int two, int three) {
        int min = one;
        if (two < min) {
            min = two;
        }
        if (three < min) {
            min = three;
        }
        return min;
    }
 
    /**
     * 计算两个字符串的“编辑距离”
     * @param str1
     * @param str2
     * @return
     */
    private static int ld(String str1, String str2) {
        int d[][]; // 矩阵
        int n = str1.length();
        int m = str2.length();
        int i; // 遍历str1的
        int j; // 遍历str2的
        char ch1; // str1的
        char ch2; // str2的
        int temp; // 记录相同字符,在某个矩阵位置值的增量,不是0就是1
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new int[n + 1][m + 1];
        for (i = 0; i <= n; i++) { // 初始化第一列
            d[i][0] = i;
        }
        for (j = 0; j <= m; j++) { // 初始化第一行
            d[0][j] = j;
        }
        for (i = 1; i <= n; i++) { // 遍历str1
            ch1 = str1.charAt(i - 1);
            // 去匹配str2
            for (j = 1; j <= m; j++) {
                ch2 = str2.charAt(j - 1);
                if (ch1 == ch2) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                // 左边+1,上边+1, 左上角+temp取最小
                d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1]+ temp);
            }
        }
        return d[n][m];
    }
    
    /**
     * 字符串相似度，根据编辑距离计算相似度(0 ~ 1之间，1为完全相同的两个字符串)
     * @param str1
     * @param str2
     * @return
     */
    public static double sim(String str1, String str2) {
        try {
            double ld = (double)ld(str1, str2);
            return (1-ld/(double)Math.max(str1.length(), str2.length()));
        } catch (Exception e) {
            return 0.1;
        }
    }
    
    /**
     * 搜索匹配度
     * @param src
     * @param des
     * @return
     */
    public static double simAtoB(String src, String des) {
        try {
            double ld = (double)ld(src, des);
            
            // TODO
            if (src.startsWith(des)) { // 以目标字符串为起始则提高匹配度到0.9以上
            	return 1 - 0.1 * (ld/(double)Math.max(src.length(), des.length()));
            } else if (src.contains(des)) { // 包含目标字符串则提高匹配度到0.8以上
            	return 1 - 0.2 * (ld/(double)Math.max(src.length(), des.length()));
            }
            
            return (1-ld/(double)Math.max(src.length(), des.length()));
        } catch (Exception e) {
            return 0.1;
        }
    }
}
