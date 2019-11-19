package com.tsystems.javaschool.medical.backend.services;

import com.tsystems.javaschool.medical.backend.component.EventStatusChangerImpl;
import com.tsystems.javaschool.medical.backend.dao.EventRepository;
import com.tsystems.javaschool.medical.backend.dto.*;
import com.tsystems.javaschool.medical.backend.dto.enums.MsgStatus;
import com.tsystems.javaschool.medical.backend.entities.EventsEntity;
import com.tsystems.javaschool.medical.backend.exception.EventStatusChangerExeption;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventsService {

    private static final Logger Logger = LogManager.getLogger(EventsService.class);
    private final ModelMapper modelMapper;
    private final EventRepository eventRepository;
    private final EventStatusChangerImpl eventStatusChanger;

    @Autowired
    public EventsService(ModelMapper modelMapper, EventRepository eventRepository, EventStatusChangerImpl eventStatusChanger) {
        this.modelMapper = modelMapper;
        this.eventRepository = eventRepository;
        this.eventStatusChanger = eventStatusChanger;
    }

    @Transactional(readOnly = true)
    public EventListResponse getEventsList(int start, int length, String orderBy, String orderDir) {

        EventListResponse eventListResponse = new EventListResponse();
        List<EventsDto> eventsList = new ArrayList<>();
        List<EventsEntity> eventsEntityList = eventRepository.getAll(start, length, orderBy, orderDir);
        long count = eventRepository.getCount();

        for (Object a : eventsEntityList) {
            EventsDto eventsDto = modelMapper.map(a, EventsDto.class);
            eventsList.add(eventsDto);
        }

        eventListResponse.setList(eventsList);
        eventListResponse.setRecords(count);
        return eventListResponse;
    }

    public void addEvent(EventRequestDto params) {
        eventRepository.create(params);
    }

    public void deleteEvent(int id) {
        eventRepository.delete(id);
    }

    public EventUpdateDto updateEvent(EventRequestDto params) {
        EventUpdateDto eventUpdateDto = new EventUpdateDto();
        List<MsgDto> msgDtoList = new ArrayList<>();
        MsgDto msgDto = new MsgDto();
        try {
            eventStatusChanger.changeStatus(params.getId(), params.getStatus());
            eventRepository.update(params);
        }
        catch (EventStatusChangerExeption e){
            msgDto.setMessage(e.getMessage());
            msgDto.setStatus(MsgStatus.ERROR);
            msgDtoList.add(msgDto);
            eventUpdateDto.setMsg(msgDtoList);
            Logger.info(e.getMessage());
        }
        return eventUpdateDto;
    }
}
