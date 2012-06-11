package FluxCapacitor.services;

import FluxCapacitor.util.FluxConfiguration;
import LASER.App;
import com.google.gson.Gson;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.codehaus.jettison.json.JSONException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashMap;


@Path("laser")
public class Laser {

    private class LaserThread extends Thread {
        private String[] args;

        public LaserThread(String[] args) {
            this.args = args;
        }

        @Override
        public void run() {
            try {
                App.main(args);
            } catch (Exception e) {
                System.err.print("Running recommendation job failed.");
                e.printStackTrace();
            }
        }
    }

    @POST
    @Path("startrecommendation")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void startRecommendation(@FormParam("nameNode") String nameNode,
                                    @FormParam("jobTracker") String jobTracker,
                                    @FormParam("simType") String simType,
                                    @FormParam("numSim") String numSim,
                                    @Context HttpServletResponse servletResponse) throws IOException {

        final String[] args = new String[]{nameNode, jobTracker, simType, numSim};

        LaserThread lt = new LaserThread(args);

        lt.start();

        servletResponse.setStatus(200);
        servletResponse.sendRedirect("/");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("clusterstatus")
    public String getClusterStatus(@Context HttpServletResponse servletResponse) throws IOException, JSONException {
        FluxConfiguration flux = FluxConfiguration.getInstance();

        if (!flux.hasConfig("jobtracker")) {
            throw new InternalError("config missing");
        }

        JobConf jobConf = new JobConf();
        jobConf.set("mapred.job.tracker", flux.get("jobtracker"));

        ClusterStatus cluster = null;

        try {
            JobClient jobClient = new JobClient(jobConf);
            cluster = jobClient.getClusterStatus();
        } catch (IOException e) {
                servletResponse.setStatus(500);
                return "";
        }

        servletResponse.setStatus(200);
        return new Gson().toJson(cluster);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("datastatus")
    public String getDataStatus(@Context HttpServletResponse servletResponse) throws IOException {
        FluxConfiguration flux = FluxConfiguration.getInstance();
        Configuration conf = new Configuration();
        conf.set("fs.default.name", flux.get("hdfs"));

        FileSystem fs = FileSystem.get(conf);
        org.apache.hadoop.fs.Path pathInput = new org.apache.hadoop.fs.Path("Laser/input");
        org.apache.hadoop.fs.Path pathOutput = new org.apache.hadoop.fs.Path("Laser/output");

        HashMap<String, Object> results = new HashMap<String, Object>();

        try {
            FileStatus[] stats = fs.listStatus(pathInput);

            for (FileStatus stat : stats) {
                long totalInputSize = results.containsKey("inputSize") ? (Long) results.get("inputSize") : 0;
                totalInputSize += stat.getLen();
                results.put("inputSize", totalInputSize);
            }
        } catch (IOException e) {
            servletResponse.setStatus(500);
            return new Gson().toJson("");
        }

        long outputSize = 0;

        try {
            FileStatus[] stats = fs.listStatus(pathOutput);
            if (stats != null) {
                for (FileStatus stat : stats) {
                    outputSize = results.containsKey("outputSize") ? (Long) results.get("outputSize") : 0;
                    if (!stat.isDir()) {
                        outputSize += stat.getLen();

                    }
                }
            } else {
                throw new IOException();
            }
        } catch (IOException e) {
            servletResponse.setStatus(500);
            return new Gson().toJson("");

        } finally {
            results.put("outputSize", outputSize);
        }


        return new Gson().toJson(results);
    }
}
