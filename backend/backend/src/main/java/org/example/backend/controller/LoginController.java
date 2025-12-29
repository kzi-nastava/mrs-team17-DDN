package org.example.backend.controller;

import org.example.backend.dto.request.LoginRequestDto;
import org.example.backend.dto.response.LoginResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController//namenjen za obradu HTTP zahteva i vraćanje HTTP odgovora i rad sa RESTful web servisima
@RequestMapping("/api/login") //odjredjuje url putnaju mapira HTTP zahteve na određene metode kontrolera ove tri notacije znace da je rest kontroler i da svi njegovi endpointi pocinju sa /api/login
public class LoginController {  //kontroler za rukovanje prijavom korisnika

    @PostMapping //mapira HTTP POST zahteve na metodu login kada korisnike salje post zaheb za izmenu ili kreianje novog resursa    
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) { //prima LoginRequestDto iz tela zahteva
        //  logika autentifikacije
        return ResponseEntity.ok(new LoginResponseDto());// vraća uspešan odgovor sa LoginResponseDto trenutno nista 
        //ideja je da vrati token i id mozdda
    }
}

//@RequestBody znači: preuzmi JSON iz requesta i mapiraj ga u LoginRequestDto
//– request je objekat → sadrži email i password koje je korisnik poslao    
//– vraća ResponseEntity<LoginResponseDto> → znači vraćamo neki JSON sa tokenom