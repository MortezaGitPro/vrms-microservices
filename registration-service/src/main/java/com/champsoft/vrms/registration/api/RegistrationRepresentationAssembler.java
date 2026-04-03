package com.champsoft.vrms.registration.api;

import com.champsoft.vrms.registration.api.dto.RegistrationResponse;
import com.champsoft.vrms.registration.api.dto.RenewRegistrationRequest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class RegistrationRepresentationAssembler {

    public EntityModel<RegistrationResponse> toModel(RegistrationResponse response) {
        EntityModel<RegistrationResponse> model = EntityModel.of(response);

        model.add(linkTo(methodOn(RegistrationController.class).get(response.id())).withSelfRel());
        model.add(linkTo(methodOn(RegistrationController.class).list()).withRel("registrations"));

        if ("ACTIVE".equalsIgnoreCase(response.status())) {
            model.add(
                    linkTo(methodOn(RegistrationController.class)
                            .renew(response.id(), new RenewRegistrationRequest(null)))
                            .withRel("renew")
            );

            model.add(
                    linkTo(methodOn(RegistrationController.class)
                            .cancel(response.id()))
                            .withRel("cancel")
            );
        }

        return model;
    }

    public CollectionModel<EntityModel<RegistrationResponse>> toCollectionModel(List<RegistrationResponse> responses) {
        List<EntityModel<RegistrationResponse>> items = responses.stream()
                .map(this::toModel)
                .toList();

        return CollectionModel.of(
                items,
                linkTo(methodOn(RegistrationController.class).list()).withSelfRel()
        );
    }
}
