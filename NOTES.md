# Notes

- Should this be implemented for ID generation: [Embedding a Primary Key in a UUID / GUID for CQRS / ES]
  (http://rbtech.blogspot.ca/2013/10/embedding-primary-key-in-uuid-guid-for.html)

# Using REST

```bash
curl http://localhost:9000/studies
curl --include --request POST --header "Content-type: application/json" \
    --data '{"type": "AddStudyCmd", "name": "ST1", "description": "ST1 Description"}' \
    http://localhost:9000/studies
curl --include --request PUT --header "Content-type: application/json" \
    --data '{"id":"BDDC7EF0-0207-46FD-B02E-BA8E403030F5", "version":0,"name": "ST1A", "description": "ST1 Description"}' \
    http://localhost:9000/studies/BDDC7EF0-0207-46FD-B02E-BA8E403030F5
curl --include --request POST --header "Content-type: application/json" \
    --data '{"id":"BDDC7EF0-0207-46FD-B02E-BA8E403030F5", "version":0}' \
    http://localhost:9000/studies/retire
```

# Learning AngularjS

https://github.com/jmcunningham/AngularJS-Learning

# Grids or Tables in AngularJS

http://stackoverflow.com/questions/21375073/best-way-to-represent-a-grid-or-table-in-angularjs-with-bootstrap-3

---

[Back to top](../README.md)
