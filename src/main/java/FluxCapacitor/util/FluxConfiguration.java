package FluxCapacitor.util;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import java.io.IOException;
import java.util.HashMap;

public class FluxConfiguration {
    private static FluxConfiguration instance = null;


    private static HashMap<String,String> config;

    private FluxConfiguration(){
        config = new HashMap<String,String>();
    }

    public void set(String key, String value) {
        config.put(key,value);
    }

    public String get(String key){
        if(config.containsKey(key)){
            return config.get(key);
        }

        throw new InternalError("configuration not found");
    }

    public boolean hasConfig(String key) {
        return config.containsKey(key);
    }

    public static FluxConfiguration getInstance(){
        if(instance == null) {
            instance = new FluxConfiguration();
        }

        return instance;
    }
}
