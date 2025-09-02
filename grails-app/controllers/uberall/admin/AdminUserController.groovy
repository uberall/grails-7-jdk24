package uberall.admin

import uberall.User
import uberall.UserService

class AdminUserController {

    UserService userService

    def create() {
        userService.create(params.email as String)
        render 'created'
    }

    def get(Long id) {
        User user = User.get(id)
        render(contentType: 'application/json', text: user.toString())
    }
}
