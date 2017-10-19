/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash'

fdescribe('membershipAddComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test')
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      Object.assign(this, ComponentTestSuiteMixin)

      this.injectDependencies('$q',
                              'UserName',
                              'Factory')

      this.createController = () => {

        ComponentTestSuiteMixin.createController.call(
          this,
          '<membership-add></membership-add>',
          undefined,
          'membershipAdd')
      }

      this.entityInfoFrom = (entity) => _.pick(entity, 'id', 'name')
    })
  })

  it('has valid scope', function() {
    this.createController()
    expect(this.controller.breadcrumbs).toBeDefined()
  })

  describe('for users', function() {

    it('gets user names', function() {
      const userNames = [ this.UserName.create(this.Factory.userNameDto()) ]
      this.UserName.list = jasmine.createSpy().and.returnValue(this.$q.when(userNames))
      this.createController()
      this.controller.getUserNames('')
        .then(reply => {
          expect(reply).toBeArrayOfSize(userNames.length)
          reply.forEach(item => {
            expect(item).toEqual(jasmine.any(this.UserName))
          })
        })
    })

    it('membership is updated when a user is selected', function() {
      const entityInfo = this.entityInfoFrom(this.Factory.user())
      this.createController()
      expect(this.controller.membership.userData).toBeEmptyArray()
      this.controller.userSelected(entityInfo)
      expect(this.controller.membership.userData).toBeArrayOfSize(1)
      expect(this.controller.membership.userData[0].id).toBe(entityInfo.id)
      expect(this.controller.membership.userData[0].name).toBe(entityInfo.name)
    })

    it('membership is updated when a user is removed', function() {
      const entityInfo = this.entityInfoFrom(this.Factory.user())
      this.createController()
      this.controller.membership.userData = [ entityInfo ]
      this.controller.removeUser(entityInfo)
      expect(this.controller.membership.userData).toBeEmptyArray()
    })

  })

  describe('for studies', function() {

  })

  describe('for centres', function() {

  })

})
