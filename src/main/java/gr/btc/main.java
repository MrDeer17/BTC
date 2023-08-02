package gr.btc;

import net.md_5.bungee.api.ChatColor;

import static java.lang.Integer.parseInt;

public class main {
    public static String genGr(String text, String startColor, String endColor) {

        StringBuilder result = new StringBuilder();

        // Разбиваем текст на символы
        char[] chars = text.toCharArray();

        // Разбиваем цвета на компоненты
        int[] startColors = hexToRgb(startColor);
        int[] endColors = hexToRgb(endColor);

        // Рассчитываем шаги для каждого компонента цвета
        double[] steps = new double[3];
        for (int i = 0; i < 3; i++) {
            steps[i] = (endColors[i] - startColors[i]) / (double) (chars.length - 1);
        }

        // Создаем градиент
        for (int i = 0; i < chars.length; i++) {
            // Рассчитываем компоненты цвета для текущего символа
            int[] currentColors = new int[3];
            for (int j = 0; j < 3; j++) {
                currentColors[j] = (int) Math.round(startColors[j] + steps[j] * i);
            }

            // Преобразуем компоненты цвета в hex-строку
            String hexColor = String.format("#%02x%02x%02x", currentColors[0], currentColors[1], currentColors[2]);

            // Добавляем символ с цветом в результат
            result.append(ChatColor.of(hexColor)).append(chars[i]);
        }

        return result.toString();
    }

    private static int[] hexToRgb(String hexColor) {
        int[] rgb = new int[3];
        rgb[0] = parseInt(hexColor.substring(1, 3), 16);
        rgb[1] = parseInt(hexColor.substring(3, 5), 16);
        rgb[2] = parseInt(hexColor.substring(5, 7), 16);
        return rgb;
    }
}
