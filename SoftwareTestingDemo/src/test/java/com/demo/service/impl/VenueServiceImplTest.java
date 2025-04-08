package com.demo.service.impl;

import com.demo.dao.VenueDao;
import com.demo.entity.Venue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VenueServiceImplTest {

    @InjectMocks
    private VenueServiceImpl venueService;

    @Mock
    private VenueDao venueDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByVenueID() {
        Venue venue = new Venue();
        venue.setVenueID(1);
        when(venueDao.getOne(1)).thenReturn(venue);

        Venue result = venueService.findByVenueID(1);
        assertNotNull(result);
        assertEquals(1, result.getVenueID());
    }

    @Test
    void testFindByVenueName() {
        Venue venue = new Venue();
        venue.setVenueName("Main Hall");
        when(venueDao.findByVenueName("Main Hall")).thenReturn(venue);

        Venue result = venueService.findByVenueName("Main Hall");
        assertNotNull(result);
        assertEquals("Main Hall", result.getVenueName());
    }

    @Test
    void testFindAllPageable() {
        Venue venue = new Venue();
        Page<Venue> venuePage = new PageImpl<>(List.of(venue));
        Pageable pageable = mock(Pageable.class);

        when(venueDao.findAll(pageable)).thenReturn(venuePage);

        Page<Venue> result = venueService.findAll(pageable);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testFindAllList() {
        List<Venue> venues = Arrays.asList(new Venue(), new Venue());
        when(venueDao.findAll()).thenReturn(venues);

        List<Venue> result = venueService.findAll();
        assertEquals(2, result.size());
    }

    @Test
    void testCreate() {
        Venue venue = new Venue();
        venue.setVenueID(42);
        when(venueDao.save(venue)).thenReturn(venue);

        int id = venueService.create(venue);
        assertEquals(42, id);
    }

    @Test
    void testUpdate() {
        Venue venue = new Venue();
        venue.setVenueID(10);

        venueService.update(venue);

        verify(venueDao, times(1)).save(venue);
    }

    @Test
    void testDelById() {
        venueService.delById(5);
        verify(venueDao, times(1)).deleteById(5);
    }

    @Test
    void testCountVenueName() {
        when(venueDao.countByVenueName("Gym")).thenReturn(3);

        int count = venueService.countVenueName("Gym");
        assertEquals(3, count);
    }
}
