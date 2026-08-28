package it.ddlsolution.testerApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class TesterApiRuota extends BaseClass {

    public static final String BASE_URL_LOCALE = "http://localhost:8083/api/ruota";

    public static final String BASE_URL = BASE_URL_LOCALE;

    @Autowired
    RestTemplateBuilder restTemplateBuilder;
    Map<String, Integer> esiti = new HashMap<>();

    public void go(String[] args) throws IOException {
        System.out.println();
        System.out.println();


        keyValueArgs = new HashMap<>();
        for (String arg : args) {
            String[] parts = arg.split("#");
            if (parts.length == 2) {
                keyValueArgs.put(parts[0].toUpperCase(), parts[1]);
            } else {
                keyValueArgs.put(parts[0].toUpperCase(), NULL);
            }
        }

        String function = DEFAULT_FUNCTION;
        if (getValueFromArgsNoFunction(ARGS_FUNCTION) != null) {
            function = getValueFromArgsNoFunction(ARGS_FUNCTION).toString();
        }
        System.out.println("Function: " + function);
        String fileConfigurazione = DEFAULT_FILECONFIGURAZIONE;
        if (getValueFromArgsNoFunction(ARGS_PATH) != null) {
            fileConfigurazione = getValueFromArgsNoFunction(ARGS_PATH).toString();
        }
        System.out.println("Uso file configurazione: " + fileConfigurazione);

        restTemplate = restTemplateBuilder.build();
        leggiValDefault(fileConfigurazione);

        if (function.equalsIgnoreCase(enumFunction.INFO.name()) || function.equals("*")) {
            testCallInfo();
        }
        if (function.equalsIgnoreCase(enumFunction.ADD_GIOCATORE.name()) || function.equals("*")) {
            testCallAddGiocatore();
        }
        if (function.equalsIgnoreCase(enumFunction.INFO.name()) || function.equals("*")) {
            testCallInfo();
        }
        if (function.equalsIgnoreCase(enumFunction.ADD_GIOCATORE_GIMMI.name()) || function.equals("*")) {
            testCallAddGiocatoreGimmi();
        }
        if (function.equalsIgnoreCase(enumFunction.REN_GIOCATORE.name()) || function.equals("*")) {
            testCallRenameGiocatore();
        }
        if (function.equalsIgnoreCase(enumFunction.INFO.name()) || function.equals("*")) {
            testCallInfo();
        }
        if (function.equalsIgnoreCase(enumFunction.DEL_GIOCATORE.name()) || function.equals("*")) {
            testCallDelGiocatore();
        }
        if (function.equalsIgnoreCase(enumFunction.INIT.name()) || function.equals("*")) {
            testCallInit();
        }
        if (function.equalsIgnoreCase(enumFunction.INFO.name()) || function.equals("*")) {
            testCallInfo();
        }
        if (function.equalsIgnoreCase(enumFunction.ADD_GIOCATORE.name()) || function.equals("*")) {
            testCallAddGiocatore();
        }
        if (function.equalsIgnoreCase(enumFunction.AVVIA_RANDOM.name()) || function.equals("*")) {
            testCallAvviaRandom();
        }
        if (function.equalsIgnoreCase(enumFunction.INFO.name()) || function.equals("*")) {
            testCallInfo();
        }
        if (function.equalsIgnoreCase(enumFunction.AVVIA.name()) || function.equals("*")) {
            testCallAvvia();
        }
        if (function.equalsIgnoreCase(enumFunction.INFO.name()) || function.equals("*")) {
            testCallInfo();
        }
        if (function.equalsIgnoreCase(enumFunction.INIT.name()) || function.equals("*")) {
            testCallInit();
        }
        if (function.equalsIgnoreCase(enumFunction.INFO.name()) || function.equals("*")) {
            testCallInfo();
        }

        System.out.println();
        System.out.println("************** RECAP **************");
        esiti.entrySet().forEach(System.out::println);
    }

    private void testCallInfo() {
        String function = enumFunction.INFO.name();
        Integer statusCode = call(new HashMap<>()
                , BASE_URL + "/game"
                , HttpMethod.GET
                , function);
        esiti.put(function, statusCode);
    }

    private void testCallAvvia() {
        String function = enumFunction.AVVIA.name();
        Integer statusCode = call(generateBody(function
                        , ARGS_ADD_NOME_GIOCATORE_GIMMI
                )
                , BASE_URL + "/game"
                , HttpMethod.POST
                , function);
        esiti.put(function, statusCode);
    }

    private void testCallAvviaRandom() {
        String function = enumFunction.AVVIA_RANDOM.name();
        Integer statusCode = call(new HashMap<>()
                , BASE_URL + "/game"
                , HttpMethod.POST
                , function);
        esiti.put(function, statusCode);
    }

    private void testCallAddGiocatore() {
        String function = enumFunction.ADD_GIOCATORE.name();
        Integer statusCode = call(generateBody(function
                        , ARGS_ADD_NOME_GIOCATORE
                )
                , BASE_URL + "/giocatore"
                , HttpMethod.POST
                , function);
        esiti.put(function, statusCode);
    }

    private void testCallAddGiocatoreGimmi() {
        String function = enumFunction.ADD_GIOCATORE_GIMMI.name();
        Integer statusCode = call(generateBody(function
                        , ARGS_ADD_NOME_GIOCATORE_GIMMI
                )
                , BASE_URL + "/giocatore"
                , HttpMethod.POST
                , function);
        esiti.put(function, statusCode);
    }
    private void testCallRenameGiocatore() {
        String function = enumFunction.REN_GIOCATORE.name();
        Integer statusCode = call(generateBody(function
                        , ARGS_NOME_GIOCATORE_NUOVO
                )
                , BASE_URL + "/giocatore/" + getValueFromArgs(ARGS_GIOCATORE_DA_RINOMINARE.codeCsv(), function)
                , HttpMethod.PUT
                , function);
        esiti.put(function, statusCode);
    }
    private void testCallDelGiocatore() {
        String function = enumFunction.DEL_GIOCATORE.name();
        Integer statusCode = call(new HashMap<>()
                , BASE_URL + "/giocatore/" + getValueFromArgs(ARGS_NOME_GIOCATORE_DEL.codeCsv(), function)
                , HttpMethod.DELETE
                , function);
        esiti.put(function, statusCode);
    }
    private void testCallInit() {
        String function = enumFunction.INIT.name();
        Integer statusCode = call(new HashMap<>()
                , BASE_URL + "/game"
                , HttpMethod.DELETE
                , function);
        esiti.put(function, statusCode);
    }



    private static final String ARGS_PATH = "path";

    private static String DEFAULT_FILECONFIGURAZIONE = "testerApiRuota.csv";
    private static String DEFAULT_FUNCTION = "*";

    private enum enumFunction {INFO, ADD_GIOCATORE, ADD_GIOCATORE_GIMMI, REN_GIOCATORE, DEL_GIOCATORE, INIT, AVVIA, AVVIA_RANDOM}


    private static final ArgBody ARGS_ADD_NOME_GIOCATORE = new ArgBody("addNomeGiocatore", "nome");
    private static final ArgBody ARGS_ADD_NOME_GIOCATORE_GIMMI = new ArgBody("addNomeGiocatoreGimmi", "nome");
    private static final ArgBody ARGS_NOME_GIOCATORE_NUOVO = new ArgBody("nomeGiocatoreNuovo", "nome");
    private static final ArgBody ARGS_GIOCATORE_DA_RINOMINARE = new ArgBody("giocatoreDaRinominare");
    private static final ArgBody ARGS_NOME_GIOCATORE_DEL = new ArgBody("nomeGiocatoreDel");

}
