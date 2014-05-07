# Notes

- Should this be implemented for ID generation: [Embedding a Primary Key in a UUID / GUID for CQRS / ES]
  (http://rbtech.blogspot.ca/2013/10/embedding-primary-key-in-uuid-guid-for.html)

# Using REST

```bash
curl --include http://localhost:9000/studies
curl --include --request POST --header "Content-type: application/json" \
    --data '{"name": "ST1", "description": "ST1 Description"}' \
    http://localhost:9000/studies
```

---

[Back to top](../README.md)
