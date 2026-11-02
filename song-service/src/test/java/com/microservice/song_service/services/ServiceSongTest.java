package com.microservice.song_service.services; 


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.microservice.song_service.ServiceSong;
import com.microservice.song_service.dto.EntitySongDTO;
import com.microservice.song_service.entities.EntitySong;
import com.microservice.song_service.exceptions.DataNotFoundException;
import com.microservice.song_service.exceptions.MetaDataExistExceptions;
import com.microservice.song_service.repositories.RepositorySong;

@ExtendWith(MockitoExtension.class)
class ServiceSongTest {

    @Mock
    private RepositorySong repositorySong;

    @InjectMocks
    private ServiceSong serviceSong;

    @Test
    void saveSong_Success() {
        EntitySongDTO dto = new EntitySongDTO();
        dto.setId(1L);
        dto.setName("Song Name");
        dto.setArtist("Artist");

        when(repositorySong.existsById(1L)).thenReturn(false);

        Long savedId = serviceSong.saveSong(dto);

        assertEquals(1L, savedId);
        verify(repositorySong, times(1)).save(any(EntitySong.class));
    }

    @Test
    void saveSong_ThrowsExceptionWhenExists() {
        EntitySongDTO dto = new EntitySongDTO();
        dto.setId(1L);

        when(repositorySong.existsById(1L)).thenReturn(true);

        assertThrows(MetaDataExistExceptions.class, () -> serviceSong.saveSong(dto));
        verify(repositorySong, never()).save(any());
    }

    @Test
    void getSongById_NotFound() {
        when(repositorySong.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> serviceSong.getSongById(99L));
    }
}
