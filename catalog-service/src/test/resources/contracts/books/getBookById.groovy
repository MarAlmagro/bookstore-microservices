package contracts.books

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return book by ID"
    request {
        method GET()
        url "/api/v1/books/1"
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            id: 1,
            isbn: $(anyNonBlankString()),
            title: $(anyNonBlankString()),
            author: $(anyNonBlankString()),
            price: $(anyDouble()),
            stock: $(anyInteger())
        ])
    }
}
