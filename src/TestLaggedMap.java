
public class TestLaggedMap {
    public static void main(String[] args) throws InterruptedException {

        LaggedMap<String, String> map = new LaggedMap<>(2);

        System.out.println("Step 1 add value 'Apple'...");
        map.put("fruit", "Apple");


        System.out.println("Step 2: get value after put: " + map.get("fruit"));

        System.out.println("Strp 3: waiting 3 sec...");
        Thread.sleep(3000);


        System.out.println("Step 4:  get value right after delay:  " + map.get("fruit"));


        System.out.println("Step 5: Remove key...");
        map.remove("fruit", true);
        System.out.println("Get reight after remove : " + map.get("fruit"));

        Thread.sleep(3000);
        System.out.println("get after remove (after waiting): " + map.get("fruit"));
    }
}
