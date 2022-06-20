package com.example.textConvert.Controller;

import com.example.textConvert.model.Script;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Controller
public class converter {


    private final String token = "API KEY";


    private Script script = new Script();


    private static  String id;

    private Gson gson = new Gson();

    private String jsonRequest;



    @GetMapping("/")
    public ModelAndView index(){
        ModelAndView mv = new ModelAndView("index");
        script.setStatus("completed");
        mv.addObject("status", script.getStatus());
        return mv;
    }



    @PostMapping("/")
    public ModelAndView Translate(Script script, Model model) throws URISyntaxException, IOException, InterruptedException {
        ModelAndView mv = new ModelAndView("/index");

        // Getting audio URL from form and adding it to a Json format
        script.setAudio_url(script.getAudio_url());
        jsonRequest = gson.toJson(script);


        // Creating API POST REQUEST TO TRANSLATE URL INSERTED IN FORM.
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI("http://api.assemblyai.com/v2/transcript"))
                .header("Authorization", token).POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

        // Turning Json response into an object of class Script
        script = gson.fromJson(postResponse.body(), Script.class);




        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI("http://api.assemblyai.com/v2/transcript/" + script.getId()))
                .header("Authorization", token)
                .build();



        while(true){
            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
            script = gson.fromJson(getResponse.body(), Script.class);

            if("completed".equals(script.getStatus()) || "error".equals(script.getStatus())){
                break;
            }else if("processing".equals(script.getStatus())){
                mv.addObject("status", script.getStatus());
                Thread.sleep(1000);
                model.addAttribute("status", script.getStatus());
            }

        }
        mv.addObject("text", script.getText());
        return mv;

    }


}
