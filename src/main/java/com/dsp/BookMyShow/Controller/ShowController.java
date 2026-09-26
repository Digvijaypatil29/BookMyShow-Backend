package com.dsp.BookMyShow.Controller;

import com.dsp.BookMyShow.Service.CatalogService;
import com.dsp.BookMyShow.dto.ShowResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shows")
public class ShowController {

    private final CatalogService catalogService;

    public ShowController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<ShowResponse> shows(@RequestParam String city, @RequestParam LocalDate date){
        return catalogService.shows(city, date);
    }

}
