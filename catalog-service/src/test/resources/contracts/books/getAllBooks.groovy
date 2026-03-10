package contracts.books

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return list of all books"
    request {
        method GET()
        url "/api/v1/books"
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            [
                id: $(anyInteger()),
                isbn: $(anyNonBlankString()),
                title: $(anyNonBlankString()),
                author: $(anyNonBlankString()),
                price: $(anyDouble()),
                stock: $(anyInteger())
            ]
        ])
    }
}
