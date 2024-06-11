import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.*;
import java.io.*;

class CouponMeal {
    private String price;
    private String code;
    private String food_img_url;
    private String[] items;
    private String items_str;

    public void CouponMeal() {
        price = "";
        code = "";
        food_img_url = "";
        items = null;
        items_str = "";
    }

    public void setupMeal(String url, String img_url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            System.out.println("fail to connect to coupon url.");
        }

        Element price = doc.selectFirst("span.small-price");
        this.price = price.text().substring(1).replaceAll(",", "");

        Element code = doc.selectFirst("h1.combo-flow__header-title.mealsTitle");
        this.code = code.text().replaceAll("[^0-9]", "");

        this.food_img_url = img_url;
    }

    public void setItems(String text) {
        this.items = text.split(",");
        this.items_str = text;
    }

    public String[] getItems() {
        return items;
    }

    public String getPrice() {
        return price;
    }

    public String getCode() {
        return code;
    }

    public String getFood_img_url() {
        return food_img_url;
    }

    public String toString() {
        String str = "[ code: " + code + ", price: " + price + ", image url: " + food_img_url + " ]\n" + "[" + items_str
                + "]\n";
        return str;
    }

    public static void setupContents(Map<String, CouponMeal> map) {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream("data/meals.csv"), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                String code = line.substring(0, 5);
                CouponMeal m = map.get(code);
                if (m == null) {
                    continue;
                }
                m.setItems(line.substring(6, line.length()));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printMeals(List<CouponMeal> meals) {
        for (CouponMeal meal : meals) {
            System.out.println(meal.toString());
        }
    }
}

class CouponMealLinkConstructor {
    // all coupons here
    public static final String KFC_COUPON_URL = "https://www.kfcclub.com.tw/Coupon";

    // to get images with their descriptions
    public static final String KFC_PARTIAL_COUPON_IMG_URL = "https://kfcoosfs.kfcclub.com.tw/";

    // it need fCode to direct to a specific meal
    public static final String KFC_PARTIAL_MEAL_URL = "https://www.kfcclub.com.tw/meal/";

    public static List<String> setup_meal_url() throws Exception {
        Document doc = Jsoup.connect(KFC_COUPON_URL).get();
        Elements scripts = doc.select("script");
        for (Element script : scripts) {
            String content = script.html().trim();

            int start_idx = content.indexOf("coupondata: [");
            if (start_idx != -1) {
                int end_idx = content.indexOf("]", start_idx) + 1;

                String jsonPart = content.substring(start_idx + "coupondata: ".length(), end_idx);

                Pattern p = Pattern
                        .compile("\"ImgNameNew\"\s*:\s*\"([^\"]+)\",[\\s\\n\\r]*\"Fcode\"\s*:\s*\"([^\"]+)\"");
                Matcher m = p.matcher(jsonPart);

                List<String> meals = new ArrayList<>();
                while (m.find()) {
                    String link = KFC_PARTIAL_MEAL_URL + m.group(2);
                    String img_url = KFC_PARTIAL_COUPON_IMG_URL + m.group(1);
                    meals.add(link + "," + img_url);
                }
                return meals;
            }
        }
        return null;
    }

}

public class App {
    public static void main(String[] args) throws Exception {

        List<String> meal_urls_and_imgs = CouponMealLinkConstructor.setup_meal_url();

        List<CouponMeal> meals = new ArrayList<>();

        Map<String, CouponMeal> map = new HashMap<>();

        for (String meal_url_img : meal_urls_and_imgs) {
            // produce all coupon Meal instances
            CouponMeal meal = new CouponMeal();
            String[] str = meal_url_img.split(",");
            meal.setupMeal(str[0], str[1]);
            meals.add(meal);
            map.put(meal.getCode(), meal);
        }

        CouponMeal.setupContents(map);

        CouponMeal.printMeals(meals);
    }
}
