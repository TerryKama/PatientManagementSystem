package com.clinic.api.controllers;

import com.clinic.api.models.Appointment;
import com.clinic.api.services.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")

public class AppointmentController {
    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/")
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }
    @GetMapping("/{id}")
    public Optional<Appointment> getAppointmentById(@PathVariable Integer id) {
        return appointmentService.getAppointmentById(id);
    }
    @PostMapping("/")
    public Appointment createAppointment(@Valid @RequestBody Appointment appointment) {
        return appointmentService.saveAppointment(appointment);
    }
    @PutMapping("/{id}")
    public Appointment updateAppointmentById(@PathVariable Integer id, @RequestBody Appointment appointment) {
        // Should verify id matches appointment.getId() before saving
        if(!id.equals(appointment.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID in path and body don't match");
        }
        return appointmentService.saveAppointment(appointment);
    }
    @DeleteMapping("/{id}")
    public void deleteAppointmentById(@PathVariable Integer id) {
        appointmentService.deleteAppointment(id);
    }
}
