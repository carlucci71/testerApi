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
    private final String SPICCHIO = "SPICCHIO";

    public static final String BASE_URL = BASE_URL_LOCALE;

    private String function;
    
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

        function = DEFAULT_FUNCTION;
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
/*
        //testCallInfo();
        testCallAddGiocatore();
        //testCallInfo();
        testCallAddGiocatoreGimmi();
        testCallRenameGiocatore();
        //testCallInfo();
        testCallDelGiocatore();
        testCallInit();
        //testCallInfo();
        testCallAddGiocatore();
        testCallAvviaRandom();
        //testCallInfo();
        testCallAvvia();
        //testCallInfo();
        testCallInit();
        //testCallInfo();
        testCallGira();
//        testCallGiraLoop();
        testCallAvvia();
        testCallConsonante();
        //testCallInfo();
        testCallGiraConsonante();
        //testCallInfo();
        testCallGiraConsonante( 'L');
        //testCallInfo();

 */
        testCallInit();
        testCallAddGiocatore();
        testCallAvvia();
        testCallGiraChiamaForzato("100","R");
        testCallGiraChiamaForzato("200","L");
        testCallGiraForzato("PASSA");
        testCallGiraChiamaForzato("200","T");
        testCallGiraChiamaForzato("JOLLY","M");
        testCallGiraForzato("BANCAROTTA");
        testCallGiraForzato("BANCAROTTA");
        testCallGiraChiamaForzato("GARAGE","N");
        testCallGiraChiamaForzato("TRIPLO","C");
        testCallCompra();
        testCallCompraForzato("O");
        testCallCompraForzato("I");
        testCallCompraForzato("A");
        testCallCompraForzato("U");
        testCallSoluzione("Tra gennaio e marzo nel calendario di velocita e dell'amore");
        testCallSoluzione("Tra gennaio e marzo nel calendario di velociTà e dell'amore");
        testCallGiraChiamaForzato("100","R");



        System.out.println();
        System.out.println("************** RECAP **************");
        esiti.entrySet().forEach(System.out::println);
    }

    private void testCallInfo() {
        String functionCall = enumFunction.INFO.name();
        if (function.equalsIgnoreCase(functionCall) || function.equals("*")) {
            Integer statusCode = (Integer) call(new HashMap<>()
                    , BASE_URL + "/game"
                    , HttpMethod.GET
                    , functionCall).get(STATUS);
            esiti.put(functionCall, statusCode);
        }
    }

    private void testCallGiraLoop() {
        String functionCall = enumFunction.GIRA_LOOP.name();
        if (function.equalsIgnoreCase(functionCall) || function.equals("*")) {
            int conta = 0;
            Map<Object, Integer> res = new HashMap<>();
            do {
                Map mapCall = (Map) call(new HashMap<>()
                        , BASE_URL + "/game/gira"
                        , HttpMethod.GET
                        , functionCall);
                if (mapCall.get(STATUS).equals(200)) {
                    Map content = (Map) mapCall.get(CONTENT);
                    Object result = content.get(SPICCHIO);
                    res.merge(result, 1, Integer::sum);
                }


            } while (conta++ < 10000);
            System.out.println(res);
        }
    }

    private void testCallGira() {
        String functionCall = enumFunction.GIRA.name();
        if (function.equalsIgnoreCase(functionCall) || function.equals("*")) {
            Integer statusCode = (Integer) call(new HashMap<>()
                    , BASE_URL + "/game/gira"
                    , HttpMethod.GET
                    , functionCall).get(STATUS);
            esiti.put(functionCall, statusCode);
        }
    }

    private void testCallGiraChiamaForzato(String forzato, String chiama) {
        String functionCall = enumFunction.GIRA_CHIAMA_FORZATO.name();
        if (function.equalsIgnoreCase(functionCall) || function.equals("*")) {
            Map gira = (Map) call(new HashMap<>()
                    , BASE_URL + "/game/gira"
                            + "?forzato=" + forzato
                    , HttpMethod.GET
                    , functionCall).get(CONTENT);
            Integer statusCode = (Integer) call(new HashMap<>()
                    , BASE_URL + "/game/consonante"
                            + "?consonante=" + chiama
                            + "&trovato=" + gira.get(SPICCHIO)
                    , HttpMethod.GET
                    , functionCall).get(STATUS);
            esiti.put(functionCall, statusCode);
        }
    }

    private void testCallGiraForzato(String forzato) {
        String functionCall = enumFunction.GIRA_FORZATO.name();
        if (function.equalsIgnoreCase(functionCall) || function.equals("*")) {
            Integer statusCode = (Integer)  call(new HashMap<>()
                    , BASE_URL + "/game/gira"
                            + "?forzato=" + forzato
                    , HttpMethod.GET
                    , functionCall).get(STATUS);
            esiti.put(functionCall, statusCode);
        }
    }

    private void testCallCompraForzato(String forzato) {
        String functionCall = enumFunction.COMPRA_FORZATO.name();
        if (function.equalsIgnoreCase(functionCall) || function.equals("*")) {
            Integer statusCode = (Integer)  call(new HashMap<>()
                    , BASE_URL + "/game/vocale"
                            + "?vocale=" + forzato
                    , HttpMethod.GET
                    , functionCall).get(STATUS);
            esiti.put(functionCall, statusCode);
        }
    }

    private void testCallSoluzione(String forzato) {
        String functionCall = enumFunction.SOLUZIONE.name();
        if (function.equalsIgnoreCase(functionCall) || function.equals("*")) {
            Integer statusCode = (Integer)  call(new HashMap<>()
                    , BASE_URL + "/game/soluzione"
                            + "?soluzione=" + forzato
                    , HttpMethod.GET
                    , functionCall).get(STATUS);
            esiti.put(functionCall, statusCode);
        }
    }

    private void testCallCompra() {
        String functionCall = enumFunction.COMPRA.name();
        if (function.equalsIgnoreCase(functionCall) || function.equals("*")) {
            Integer statusCode = (Integer)  call(new HashMap<>()
                    , BASE_URL + "/game/vocale"
                            + "?vocale=" + getValueFromArgs(ARGS_VOCALE_COMPRA.codeCsv(), functionCall)
                    , HttpMethod.GET
                    , functionCall).get(STATUS);
            esiti.put(functionCall, statusCode);
        }
    }

    private void testCallGiraConsonante(char... c) {
        String functionCall = enumFunction.GIRA_CONSONANTE.name();
        if (function.equalsIgnoreCase(functionCall) || function.equals("*")) {
            String chiama;
            if (c.length > 0) {
                chiama = String.valueOf(c[0]);
            } else {
                chiama = (String) getValueFromArgs(ARGS_CONSONANTE_CHIAMA.codeCsv(), functionCall);
            }
            Map gira = (Map) call(new HashMap<>()
                    , BASE_URL + "/game/gira"
                    , HttpMethod.GET
                    , functionCall).get(CONTENT);
            Integer statusCode = (Integer) call(new HashMap<>()
                    , BASE_URL + "/game/consonante"
                            + "?consonante=" + chiama
                            + "&trovato=" + gira.get(SPICCHIO)
                    , HttpMethod.GET
                    , functionCall).get(STATUS);
            esiti.put(functionCall, statusCode);
        }
    }

    private void testCallConsonante() {
        String functionCall = enumFunction.CONSONANTE.name();
        if (function.equalsIgnoreCase(functionCall) || function.equals("*")) {
            Integer statusCode = (Integer) call(new HashMap<>()
                    , BASE_URL + "/game/consonante?consonante=n&trovato=200"
                    , HttpMethod.GET
                    , functionCall).get(STATUS);
            esiti.put(functionCall, statusCode);
        }
    }

    private void testCallAvvia() {
        String functionCall = enumFunction.AVVIA.name();
        if (function.equalsIgnoreCase(functionCall) || function.equals("*")) {
            Integer statusCode = (Integer) call(generateBody(functionCall
                            , ARGS_ADD_NOME_GIOCATORE_GIMMI
                    )
                    , BASE_URL + "/game"
                    , HttpMethod.POST
                    , functionCall).get(STATUS);
            esiti.put(functionCall, statusCode);
        }
    }

    private void testCallAvviaRandom() {
        String functionCall = enumFunction.AVVIA_RANDOM.name();
        if (function.equalsIgnoreCase(functionCall) || function.equals("*")) {
            Integer statusCode = (Integer) call(new HashMap<>()
                    , BASE_URL + "/game"
                    , HttpMethod.POST
                    , functionCall).get(STATUS);
            esiti.put(functionCall, statusCode);
        }
    }

    private void testCallAddGiocatore() {
        String functionCall = enumFunction.ADD_GIOCATORE.name();
        if (function.equalsIgnoreCase(functionCall) || function.equals("*")) {
            Integer statusCode = (Integer) call(generateBody(functionCall
                            , ARGS_ADD_NOME_GIOCATORE
                    )
                    , BASE_URL + "/giocatore"
                    , HttpMethod.POST
                    , functionCall).get(STATUS);
            esiti.put(functionCall, statusCode);
        }
    }

    private void testCallAddGiocatoreGimmi() {
        String functionCall = enumFunction.ADD_GIOCATORE_GIMMI.name();
        if (function.equalsIgnoreCase(functionCall) || function.equals("*")) {
            Integer statusCode = (Integer) call(generateBody(functionCall
                            , ARGS_ADD_NOME_GIOCATORE_GIMMI
                    )
                    , BASE_URL + "/giocatore"
                    , HttpMethod.POST
                    , functionCall).get(STATUS);
            esiti.put(functionCall, statusCode);
        }
    }

    private void testCallRenameGiocatore() {
        String functionCall = enumFunction.REN_GIOCATORE.name();
        if (function.equalsIgnoreCase(functionCall) || function.equals("*")) {
            Integer statusCode = (Integer) call(generateBody(functionCall
                            , ARGS_NOME_GIOCATORE_NUOVO
                    )
                    , BASE_URL + "/giocatore/" + getValueFromArgs(ARGS_GIOCATORE_DA_RINOMINARE.codeCsv(), functionCall)
                    , HttpMethod.PUT
                    , functionCall).get(STATUS);
            esiti.put(functionCall, statusCode);
        }
    }

    private void testCallDelGiocatore() {
        String functionCall = enumFunction.DEL_GIOCATORE.name();
        if (function.equalsIgnoreCase(functionCall) || function.equals("*")) {
            Integer statusCode = (Integer) call(new HashMap<>()
                    , BASE_URL + "/giocatore/" + getValueFromArgs(ARGS_NOME_GIOCATORE_DEL.codeCsv(), functionCall)
                    , HttpMethod.DELETE
                    , functionCall).get(STATUS);
            esiti.put(functionCall, statusCode);
        }
    }

    private void testCallInit() {
        String functionCall = enumFunction.INIT.name();
        if (function.equalsIgnoreCase(functionCall) || function.equals("*")) {
            Integer statusCode = (Integer) call(new HashMap<>()
                    , BASE_URL + "/game"
                    , HttpMethod.DELETE
                    , functionCall).get(STATUS);
            esiti.put(functionCall, statusCode);
        }
    }


    private static final String ARGS_PATH = "path";

    private static String DEFAULT_FILECONFIGURAZIONE = "testerApiRuota.csv";
    private static String DEFAULT_FUNCTION = "*";

    private enum enumFunction {INFO, ADD_GIOCATORE, ADD_GIOCATORE_GIMMI, REN_GIOCATORE, DEL_GIOCATORE, INIT, AVVIA, AVVIA_RANDOM, GIRA, GIRA_LOOP, CONSONANTE, GIRA_CONSONANTE, GIRA_CHIAMA_FORZATO, GIRA_FORZATO, COMPRA_FORZATO, COMPRA, SOLUZIONE}


    private static final ArgBody ARGS_ADD_NOME_GIOCATORE = new ArgBody("addNomeGiocatore", "nome");
    private static final ArgBody ARGS_CONSONANTE_CHIAMA = new ArgBody("consonante");
    private static final ArgBody ARGS_VOCALE_COMPRA = new ArgBody("vocale");
    private static final ArgBody ARGS_ADD_NOME_GIOCATORE_GIMMI = new ArgBody("addNomeGiocatoreGimmi", "nome");
    private static final ArgBody ARGS_NOME_GIOCATORE_NUOVO = new ArgBody("nomeGiocatoreNuovo", "nome");
    private static final ArgBody ARGS_GIOCATORE_DA_RINOMINARE = new ArgBody("giocatoreDaRinominare");
    private static final ArgBody ARGS_NOME_GIOCATORE_DEL = new ArgBody("nomeGiocatoreDel");

}
