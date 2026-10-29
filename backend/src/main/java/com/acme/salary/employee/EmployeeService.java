package com.acme.salary.employee;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public EmployeeResponse get(Long id) {
        return EmployeeResponse.from(find(id));
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        Employee employee = new Employee(
                request.fullName().trim(), request.jobTitle().trim(), request.department().trim(),
                request.country(), request.salary(), request.employmentType(), request.hireDate());
        return EmployeeResponse.from(repository.save(employee));
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = find(id);
        employee.setFullName(request.fullName().trim());
        employee.setJobTitle(request.jobTitle().trim());
        employee.setDepartment(request.department().trim());
        employee.setCountry(request.country());
        employee.setSalary(request.salary());
        employee.setEmploymentType(request.employmentType());
        employee.setHireDate(request.hireDate());
        // managed entity: changes are flushed on commit, no explicit save needed
        return EmployeeResponse.from(employee);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EmployeeNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private Employee find(Long id) {
        return repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
    }
}
