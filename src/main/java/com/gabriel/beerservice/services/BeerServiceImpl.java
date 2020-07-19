package com.gabriel.beerservice.services;

import com.gabriel.beerservice.domain.Beer;
import com.gabriel.beerservice.repositories.BeerRepository;
import com.gabriel.beerservice.web.controller.NotFoundException;
import com.gabriel.beerservice.web.mappers.BeerMapper;
import com.gabriel.beerservice.web.model.BeerDto;
import com.gabriel.beerservice.web.model.BeerPagedList;
import com.gabriel.beerservice.web.model.BeerStyleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeerServiceImpl implements BeerService {

    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;

    @Override
    public BeerPagedList listBeers(String beerName, BeerStyleEnum beerStyle, Pageable pageable){

        Page<Beer> beers = beerRepository.findAll(pageable);

        List<BeerDto> beerDtoList = beers.stream()
                .filter(beer-> isBeerName(beerName, beer))
                .filter(beer -> isBeerStyle(beerStyle, beer))
                .map(beerMapper::beerToBeerDto)
                .collect(Collectors.toList());

        return new BeerPagedList(beerDtoList,
                PageRequest.of(
                        beers.getPageable().getPageNumber(),
                        beers.getPageable().getPageSize()),
                beers.getTotalElements());
    }

    @Override
    public BeerDto getBeerById(UUID beerId) {

        return beerRepository.findById(beerId)
                .map(beer -> {
                    System.out.println("test");
                    System.out.println(beer.getBeerName());
                    return beer;
                })
                .map(beerMapper::beerToBeerDto)
                .orElseThrow(NotFoundException::new);
    }

    @Override
    public BeerDto saveBeer(BeerDto beerDto) {
        Beer beer = beerMapper.beerDtoToBeer(beerDto);
        Beer savedBeer = beerRepository.save(beer);
        return beerMapper.beerToBeerDto(savedBeer);
    }

    @Override
    public BeerDto updateBeer(UUID beerId, BeerDto beerDto) {
        Beer foundBeer = beerRepository.findById(beerId)
                .map(beer -> {
                    Optional.ofNullable(beerDto.getBeerName()).ifPresent(s -> beer.setBeerName(s));
                    Optional.ofNullable(beerDto.getBeerStyle()).ifPresent(e -> beer.setBeerStyle(e.name()));
                    Optional.ofNullable(beerDto.getUpc()).ifPresent(s -> beer.setUpc(s));
                    Optional.of(beerDto.getPrice()).ifPresent(n->beer.setPrice(n));
                    return beer;
                })
                .orElseThrow(NotFoundException::new);
        return beerMapper.beerToBeerDto(beerRepository.save(foundBeer));
    }

    @Override
    public void deleteBeer(UUID beerId) {
        beerRepository.deleteById(beerId);
    }

    private boolean isBeerName(String beerName, Beer beer){
        return Optional.ofNullable(beerName)
                .map(s->s.equals(beer.getBeerName()))
                .orElse(true);
    }
    private boolean isBeerStyle(BeerStyleEnum beerStyle, Beer beer){
        return Optional.ofNullable(beerStyle)
                .map(s->s.name().equals(beer.getBeerStyle()))
                .orElse(true);
    }
}
