package org.example.vitya.microservice.api.controllers;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.vitya.microservice.api.dto.AskDto;
import org.example.vitya.microservice.api.dto.ProjectDto;
import org.example.vitya.microservice.api.exceptions.BadRequestException;
import org.example.vitya.microservice.api.exceptions.NotFoundException;
import org.example.vitya.microservice.api.factories.ProjectDtoFactory;
import org.example.vitya.microservice.store.entities.ProjectEntity;
import org.example.vitya.microservice.store.repositories.ProjectRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor // не нужно юзать конструктор и финальные поля запихивает в конструктор
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) //TODO:  все поля final и private (ЭКСПЕРИМЕНТАЛЬНО)
@Transactional
@RestController
public class ProjectController {
    // Инжектяться через @RequiredArgsConstructor
    ProjectRepository projectRepository;

    ProjectDtoFactory projectDtoFactory;

    public static final String FETCH_PROJECTS = "/api/projects"; // получить
    public static final String DELETE_PROJECT = "/api/projects/{project_id}";
    public static final String CREATE_OR_UPDATE_PROJECT = "/api/projects";

    //    public static final String CREATE_PROJECT = "/api/projects";
//    public static final String EDIT_PROJECT = "/api/projects/{project_id}";


    @GetMapping(FETCH_PROJECTS)
    public List<ProjectDto> fetchProjects(
            @RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        // Если не пусто
        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<ProjectEntity> projectStream = optionalPrefixName
                .map(projectRepository::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(() -> projectRepository.streamAllBy());

        return projectStream
                .map(projectDtoFactory::makeProjectDto)
                .collect(Collectors.toList());
    }


//    @PostMapping(CREATE_PROJECT)
//    public ProjectDto createProject(@RequestParam("project_name") String projectName) { // @RequestBody - композитный объект
//
//        if (projectName.trim().isEmpty()) {
//            throw new BadRequestException("Project name can't be empty");
//        }
//
//        projectRepository
//                .findByName(projectName)
//                .ifPresent(project -> {
//                    throw new BadRequestException(String.format("Project  \"%s\" already  exists.", projectName));
//                });
//
//        ProjectEntity project = projectRepository.saveAndFlush(
//            ProjectEntity.builder()
//                    .name(projectName)
//                    .build()
//        );
//
//        return projectDtoFactory.makeProjectDto(project);
//    }


    @PutMapping(CREATE_OR_UPDATE_PROJECT)
    public ProjectDto createOrUpdateProject(
            @RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
            @RequestParam(value = "project_name", required = false) Optional<String> optionalProjectName
            // Another params...
    ) {

        // Возвращает то что не пусто в соответствии с предикатом
        optionalProjectName = optionalProjectName.filter(name -> !name.trim().isEmpty());

        // Если id не передаётся, то мы будем создавать проект
        boolean isCreate = !optionalProjectId.isPresent();

        final ProjectEntity project = optionalProjectId
                .map(this::getProjectOrThrowException)
                .orElseGet(() -> ProjectEntity.builder().build());

        if (isCreate && !optionalProjectName.isPresent()) {
            throw new BadRequestException("Project name can't be empty");
        }

        optionalProjectName
                .ifPresent(projectName -> {

                    projectRepository
                            .findByName(projectName)
                            .filter(anotherProject -> !Objects.equals(anotherProject.getId(), project.getId()))
                            .ifPresent(anotherProject -> {
                                throw new BadRequestException(String.format("Project  \"%s\" already  exists.", projectName));
                            });

                    project.setName(projectName);
                });

        final ProjectEntity savedProject = projectRepository.saveAndFlush(project);

        return projectDtoFactory.makeProjectDto(savedProject);
    }


//    Частичная замена информации у сущности
//    @PatchMapping(EDIT_PROJECT)
//    public ProjectDto editPatch(
//        @PathVariable("project_id") Long projectId,
//        @RequestParam("project_name") String projectName
//    ) {
//
//        if (projectName.trim().isEmpty()) {
//            throw new BadRequestException("Project name can't be empty");
//        }
//
//        // Ищем проект по id
//        ProjectEntity project = getProjectOrThrowException(projectId);
//        // Вдруг новое имя уже занято
//        projectRepository
//                .findByName(projectName)
//                .filter(anotherProject -> !Objects.equals(anotherProject.getId(), projectId))
//                .ifPresent(anotherProject -> {
//                    throw new BadRequestException(String.format("Project  \"%s\" already  exists.", projectName));
//                });
//
//        project.setName(projectName);
//
//        project = projectRepository.saveAndFlush(project);
//
//        return projectDtoFactory.makeProjectDto(project);
//    }


    @DeleteMapping(DELETE_PROJECT)
    public AskDto deleteProject(@PathVariable("project_id") Long projectId) {

        getProjectOrThrowException(projectId);

        projectRepository.deleteById(projectId);

        return AskDto.makeDefault(true);
    }


    private ProjectEntity getProjectOrThrowException(Long projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Project with \"%s\" doesn't exist.",
                                        projectId
                                )
                        )
                );
    }


}
