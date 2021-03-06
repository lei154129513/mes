package com.qcadoo.mes.basic.controllers;

import com.qcadoo.mes.basic.controllers.dataProvider.DataProvider;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.DataResponse;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public final class BasicApiController {

    @Autowired
    private DataProvider dataProvider;

    @ResponseBody
    @RequestMapping(value = "/products", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataResponse getProductsByQuery(@RequestParam("query") String query) {
        return dataProvider.getProductsResponseByQuery(query);
    }

    @ResponseBody
    @RequestMapping(value = "/additionalcodes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataResponse getAdditionalCodesByQuery(@RequestParam("query") String query, @RequestParam(required = false, value = "productnumber") String productnumber) {
        return dataProvider.getAdditionalCodesResponseByQuery(query, productnumber);
    }

    @ResponseBody
    @RequestMapping(value = "/palletnumbers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataResponse getPalletNumbersByQuery(@RequestParam("query") String query) {
        return dataProvider.getPalletNumbersResponseByQuery(query);
    }

    @ResponseBody
    @RequestMapping(value = "/attribute/{attr}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataResponse getAttributesByQuery(@PathVariable String attr, @RequestParam("query") String query, HttpServletRequest httpServletRequest)
            throws UnsupportedEncodingException {
        String requestURI = httpServletRequest.getRequestURI();
        URI uri = URI.create(requestURI);
        Path path = Paths.get(uri.getPath());
        String last = path.getFileName().toString();
        return dataProvider.getAttributesByQuery(last, query);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/units")
    public List<Map<String, String>> getUnits() {
        return dataProvider.getUnits();
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/typeOfPallets")
    public List<Map<String, String>> getTypeOfPallets() {
        return dataProvider.getTypeOfPallets();
    }
}
