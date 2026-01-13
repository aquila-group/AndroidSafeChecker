package com.android.cs.checkrom.utils;

import android.widget.TextView;

import java.util.LinkedList;
import java.util.Random;

public class NumAnimUtil {
    //每秒刷新多少次
    private static final int COUNTPERS = 10;

    /**
     * 进度100  时间2秒
     * @param textV
     */
    public static void startAnim(TextView textV) {
        startAnim(textV, 100, 2000);
    }

    /**
     * 进度随意  时间2秒
     * @param textV
     */
    public static void startAnim(TextView textV, float num) {
        startAnim(textV, num, 2000);
    }

    /**
     * 进度随意  时间随意
     * @param textV
     */
    public static void startAnim(TextView textV, float num, long time) {
        if (num == 0) {
            textV.setText(NumAnimUtil.NumberFormat(num, 2));
            return;
        }

        Float[] nums = splitnum(num, (int) ((time / 1000f) * COUNTPERS));

        Counter counter = new Counter(textV, nums, time);

        textV.removeCallbacks(counter);
        textV.post(counter);
    }

    private static Float[] splitnum(float num, int count) {
        Random random = new Random();
        float numtemp = num;
        float sum = 0;
        LinkedList<Float> nums = new LinkedList<Float>();
        nums.add(0f);
        while (true) {
            float nextFloat = NumAnimUtil.NumberFormatFloat(
                    (random.nextFloat() * num * 2f) / (float) count,
                    2);
            System.out.println("next:" + nextFloat);
            if (numtemp - nextFloat >= 0) {
                sum = NumAnimUtil.NumberFormatFloat(sum + nextFloat, 2);
                nums.add(sum);
                numtemp -= nextFloat;
            } else {
                nums.add(num);
                return nums.toArray(new Float[0]);
            }
        }
    }

    static class Counter implements Runnable {

        private final TextView view;
        private Float[] nums;
        private long pertime;

        private int i = 0;

        Counter(TextView view, Float[] nums, long time) {
            this.view = view;
            this.nums = nums;
            this.pertime = time / nums.length;
        }

        @Override
        public void run() {
            if (i > nums.length - 1) {
                view.removeCallbacks(Counter.this);
                return;
            }
            view.setText("正在安全检测中("+NumAnimUtil.NumberFormat(nums[i++], 2)+"%)");
            view.removeCallbacks(Counter.this);
            view.postDelayed(Counter.this, pertime);
        }
    }

    public static String NumberFormat(float f, int m) {
        return String.format("%." + 0 + "f", f);
//        return String.format("%" + f);
    }

    public static float NumberFormatFloat(float f, int m) {
        String strfloat = NumberFormat(f, m);
        return Float.parseFloat(strfloat);
    }
}
