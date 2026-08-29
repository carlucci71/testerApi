package it.ddlsolution.testerApi;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public abstract class BaseClass {


    protected final String STATUS = "STATUS";
    protected final String CONTENT = "CONTENT";
    protected final String ARGS_FUNCTION = "function";
    protected static final String NULL = "@NULL@";
    protected final ObjectMapper mapper;

    protected record ArgBody(String codeCsv, String keyBody) {
        protected ArgBody(String codeCsv) {
            this(codeCsv, codeCsv);
        }
    }

    protected static final String ARGS_DIR_EXCEL = "dirExcel";

    protected Map<String, List<String>> valoriDefault;
    protected String token;
    protected Map<String, String> formati;
    protected RestTemplate restTemplate;

    protected Map<String, String> keyValueArgs;

    BaseClass() {
        this.mapper = jsonMapper();
    }

    protected Object getValueFromArgsNoFunction(String key) {
        return keyValueArgs.get(key.toUpperCase());
    }

    protected Object getValueFromArgs(String key, String function) {
        Object ret;
        boolean fromFile;
        key = key.toUpperCase();
        if (keyValueArgs.get(key) == null) {
            fromFile = true;
            List<String> functions = valoriDefault.get(ARGS_FUNCTION.toUpperCase());
            Integer positionFunction = null;
            if (ObjectUtils.isEmpty(valoriDefault.get(key))) {
                throw new RuntimeException(key + " non configurata");
            }
            int conta = 0;
            do {
                if (conta >= functions.size()) {
                 //   throw new RuntimeException("Function " + function + " non configurata!");
                    positionFunction=0;
                } else {
                    if (functions.get(conta).equalsIgnoreCase(function)) {
                        positionFunction = conta;
                    }
                    conta++;
                }
            } while (positionFunction == null);
            //}
            if (positionFunction >= valoriDefault.get(key).size()) {
                positionFunction = 0;
            }

            ret = valoriDefault.get(key).get(positionFunction);
            if (ObjectUtils.isEmpty(ret)) {
                ret = valoriDefault.get(key).get(0);
            }
            String formato = formati.get(key);
            if (formato != null) {
                if (formato.equals("base64encode")) {
                    ret = new String(Base64.getEncoder().encode(ret.toString().getBytes(StandardCharsets.UTF_8)));
                } else if (formato.equals("json")) {
                    try{
                        ret = fromJson(ret.toString(), Map.class);
                    } catch (Exception e) {
                        ret = fromJson(ret.toString(), List.class);
                    }
                } else if (formato.equals("array")) {
                    ret = ret.toString().split(",");
                } else if (formato.indexOf("d") > -1) {
                    ret = ret.equals("") ? "" : String.format(formato, Integer.parseInt(ret.toString()));
                } else if (formato.indexOf("s") > -1) {
                    ret = String.format(formato, ret);
                } else {
                    throw new RuntimeException("Format non gestito: " + formato);
                }
            }

        } else {
            fromFile = false;
            ret = keyValueArgs.get(key);
        }
        if (ret == null || ret.equals(NULL)) {
            return null;
        } else {
            return ret;
        }
    }

    protected Map<String, Object>  call(Object body, String url, HttpMethod httpMethod, String function) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        return call(body, headers, url, httpMethod, function);
    }

    protected Map<String, Object> call(Object body, MultiValueMap<String, String> headers, String url, HttpMethod httpMethod, String function) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start(function);
        System.out.println("====================================================================================================================");
        String token = retrieveToken(function);
        headers.add("access_token", token);
        System.out.println("TOKEN: " + token);
        headers.add("Content-Type", "application/json");
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);
        Map<String, Object> ret = new HashMap<>();
        try {
            System.out.println(function + " -> " + httpMethod + " " + url);
            System.out.println(toJson(requestEntity.getBody()));
            System.out.println("-----------------------------");
            ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, requestEntity, String.class);
            System.out.println(response.getStatusCode());
            if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
                Object o = fromJson(response.getBody(), Object.class);
                if (o instanceof Map) {
                    Map m = (Map) o;
                    if (m.get("xmlResponse") != null) {
                        System.out.println(new String(Base64.getDecoder().decode(m.get("xmlResponse").toString())));
                    }
                }
                String json = toJson(o);
                System.out.println(json);
                System.out.println();
                printHeader(response.getHeaders(), "X-Total-Count");
                printHeader(response.getHeaders(), "Link");
                printHeader(response.getHeaders(), "X-Bps*");
                ret.put(CONTENT, o);
            }
            System.out.println();
            ret.put(STATUS, response.getStatusCode().value());
        } catch (RestClientResponseException e) {
            System.out.println(e.getMessage());
            System.out.println();
            printHeader(e.getResponseHeaders(), "X-Bps*");
            System.out.println();
            ret.put(STATUS, e.getRawStatusCode());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            ret = null;
        }
        stopWatch.stop();
        System.out.println("stopWatch.prettyPrint() = " + stopWatch.getTotalTimeSeconds());
        return ret;
    }

    private static void printHeader(HttpHeaders headers, String key) {
        if (key.endsWith("*")){
            for (Map.Entry<String, String> entryHeader : headers.toSingleValueMap().entrySet()) {
                if (entryHeader.getKey().startsWith(key.substring(0,key.length()-1))) {
                    System.out.println(entryHeader.getKey() + ": " + entryHeader.getValue());
                }
            }
        } else {
            if (headers.get(key) != null) {
                headers.get(key).forEach(e -> System.out.println(key + ": " + e));
            }
        }
    }

    protected Integer callExcel(Object body, String url, HttpMethod httpMethod, String function) {
        Integer ret = null;
        System.out.println("====================================================================================================================");
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("access_token", retrieveToken(function));
        headers.add("Content-Type", "application/json");
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);
        try {
            System.out.println(function + " -> " + httpMethod + " " + url);
            System.out.println(toJson(requestEntity.getBody()));
            System.out.println("-----------------------------");
            ResponseEntity<Resource> response = restTemplate.exchange(url, httpMethod, requestEntity, Resource.class);
            System.out.println(response.getStatusCode());
            ByteArrayResource resource = (ByteArrayResource) response.getBody();
            if (resource != null) {
                byte[] fileBytes = resource.getByteArray();
                String fileName = getValueFromArgs(ARGS_DIR_EXCEL, function) + response.getHeaders().getContentDisposition().getFilename();
                Files.write(Paths.get(fileName), fileBytes);
                System.out.println("Generato:");
                System.out.println(fileName);
            }
            ret=response.getStatusCode().value();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println();
        return ret;
    }


    protected String retrieveToken(String function) {
        if (token == null) {
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
            String url = "http://localhost:8083/api/ruota/auth/token?role=ADMIN&id=1";
            var response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            if (response.getBody() == null) {
                throw new RuntimeException("Errore nel recupero del token");
            }
            token = response.getBody();
        }
        return token;
    }


    protected String toJson(Object o) {
        try {
            byte[] data = mapper.writeValueAsBytes(o);
            return new String(data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void leggiValDefault(String arg) throws IOException {
        valoriDefault = new TreeMap<>();
        formati = new HashMap<>();
        List<String> righe = Files.readAllLines(Path.of(arg));
        for (String riga : righe) {
            String[] split = riga.split(";");
            String key = split[0].toUpperCase();
            if (!key.equalsIgnoreCase(ARGS_FUNCTION)
                    && split.length > 1
                    && !ObjectUtils.isEmpty(split[1])) {
                formati.put(key, split[1]);
            }
            valoriDefault.put(key,
                    Arrays.stream(split)
                            .skip(2)
                            .collect(Collectors.toList())
            );
        }
    }

    protected Map.Entry<String, Object> getEntry(String key, String function) {
        Object valueFromArgs = getValueFromArgs(key, function);
        if (valueFromArgs == null) {
            return new AbstractMap.SimpleEntry<>(key, null);
        }
        return Map.entry(key, valueFromArgs);
    }

    protected Map.Entry<String, Object> getEntry(ArgBody key, String function) {
        Object valueFromArgs = getValueFromArgs(key.codeCsv, function);
        if (valueFromArgs == null) {
            return new AbstractMap.SimpleEntry<>(key.keyBody, null);
        }
        return Map.entry(key.keyBody, valueFromArgs);
    }

    protected MultiValueMap<String, String> generaHeader(String function, String... keys) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        for (String key : keys) {
            headers.add(getEntry(key, function).getKey().toString(), getEntry(key, function).getValue().toString());
        }
        return headers;
    }

    protected Map<String, Object> generateBody(String function, String... keys) {
        List<Map.Entry<String, Object>> entries = new ArrayList<>();
        for (String key : keys) {
        }
        Map<String, Object> ret = new HashMap<>();
        for (Map.Entry<String, Object> entry : entries) {
            ret.put(entry.getKey(), entry.getValue());
        }
        return ret;
    }

    protected Map<String, Object> generateBody(String function, ArgBody... keys) {
        List<Map.Entry<String, Object>> entries = new ArrayList<>();
        for (ArgBody key : keys) {
                entries.add(getEntry(key, function));
        }
        Map<String, Object> ret = new HashMap<>();
        for (Map.Entry<String, Object> entry : entries) {
            ret.put(entry.getKey(), entry.getValue());
        }
        return ret;
    }


    protected Map<String, Object> generateBodyFlat(List<Object[]> pairs) {
        List<Map.Entry<String, Object>> entries = new ArrayList<>();
        for (Object[] pair : pairs) {
            entries.add(new AbstractMap.SimpleEntry<>(pair[0].toString(), pair[1]));
        }

        Map<String, Object> ret = new HashMap<>();
        for (Map.Entry<String, Object> entry : entries) {
            ret.put(entry.getKey(), entry.getValue());
        }
        return ret;
    }


    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .configure(MapperFeature.USE_ANNOTATIONS, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum PortfolioAccountType {
        SBFMV(2, "2", "M"),
        SBF(1, "1", "B");

        private Integer id;
        private String code;
        private String description;

        PortfolioAccountType(Integer id, String code, String description) {
            this.id = id;
            this.code = code;
            this.description = description;
        }

        @JsonProperty
        public Integer getId() {
            return id;
        }

        @JsonProperty
        public String getCode() {
            return code;
        }

        @JsonProperty
        public String getDescription() {
            return description;
        }

        public static PortfolioAccountType getByCode(String codice) {
            for (PortfolioAccountType value : values()) {
                if (value.code.equals(codice)) return value;
            }
            return null;
        }
    }

}
