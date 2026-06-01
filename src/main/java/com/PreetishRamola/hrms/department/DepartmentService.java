package com.PreetishRamola.hrms.department;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public Department create(Department request) {
        if (departmentRepository.existsByName(request.getName()))
            throw new IllegalStateException("Department already exists: " + request.getName());
        return departmentRepository.save(request);
    }

    public List<Department> getAll() { return departmentRepository.findAll(); }

    public Department getById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));
    }

    public Department update(Long id, Department request) {
        Department dept = getById(id);
        dept.setName(request.getName());
        dept.setDescription(request.getDescription());
        dept.setHeadId(request.getHeadId());
        return departmentRepository.save(dept);
    }
}
