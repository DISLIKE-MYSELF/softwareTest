package com.demo.controller.user;

import com.demo.entity.Venue;
import com.demo.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VenueControllerTest {

    @InjectMocks
    private VenueController venueController;

    @Mock
    private VenueService venueService;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testToGymPage() {
        Venue venue = new Venue();
        venue.setVenueID(1);
        venue.setName("Test Venue");

        when(venueService.findByVenueID(1)).thenReturn(venue);

        String result = venueController.toGymPage(model, 1);

        verify(model).addAttribute("venue", venue);
        assertEquals("venue", result);
    }

    @Test
    void testVenueListJson() {
        Venue v1 = new Venue(); v1.setVenueID(1);
        Venue v2 = new Venue(); v2.setVenueID(2);
        List<Venue> venues = Arrays.asList(v1, v2);

        Page<Venue> venuePage = new PageImpl<>(venues);
        when(venueService.findAll(any(Pageable.class))).thenReturn(venuePage);

        Page<Venue> result = venueController.venue_list(1);
        assertEquals(2, result.getContent().size());
        assertEquals(1, result.getContent().get(0).getVenueID());
    }

    @Test
    void testVenueListView() {
        Venue v1 = new Venue(); v1.setVenueID(1);
        Venue v2 = new Venue(); v2.setVenueID(2);
        List<Venue> venues = Arrays.asList(v1, v2);

        Page<Venue> venuePage = new PageImpl<>(venues, PageRequest.of(0, 5), 2);
        when(venueService.findAll(any(Pageable.class))).thenReturn(venuePage);

        String viewName = venueController.venue_list(model);

        verify(model).addAttribute("venue_list", venues);
        verify(model).addAttribute("total", 1);
        assertEquals("venue_list", viewName);
    }
}
