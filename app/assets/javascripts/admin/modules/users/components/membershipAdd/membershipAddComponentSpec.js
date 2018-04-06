/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import { MembershipTestSuiteMixin } from 'test/mixins/MembershipTestSuiteMixin';
import ngModule from '../../index'

describe('membershipAddComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test')
    angular.mock.inject(function () {
      Object.assign(this, ComponentTestSuiteMixin, MembershipTestSuiteMixin)

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              '$state',
                              'biobankApi',
                              'notificationsService',
                              'domainNotificationService',
                              'UserName',
                              'StudyName',
                              'CentreName',
                              'Membership',
                              'Factory')

      this.createController = () => {
        ComponentTestSuiteMixin.createController.call(
          this,
          '<membership-add></membership-add>',
          undefined,
          'membershipAdd')
      }
    })
  })

  it('has valid scope', function() {
    this.createController()
    expect(this.controller.breadcrumbs).toBeDefined()
    expect(this.controller.$scope.membershipForm.$invalid).toBeTrue()
  })

  describe('for users', function() {

    describe('when getting users', function() {

      it('gets user names', function() {
        const userNames = [ this.UserName.create(this.Factory.userNameDto()) ]
        this.UserName.list = jasmine.createSpy().and.returnValue(this.$q.when(userNames))
        this.createController()
        this.controller.getUserNames()('')
          .then(reply => {
            expect(reply).toBeArrayOfSize(userNames.length)
            reply.forEach(item => {
              expect(item.obj).toEqual(jasmine.any(this.UserName))
            })
          })
        this.scope.$digest()
      })

      it('omits user names if they are already selected', function() {
        const rawUserNames = [ this.Factory.userNameDto(), this.Factory.userNameDto() ],
              userNames = rawUserNames.map(this.UserName.create)
        this.biobankApi.get = jasmine.createSpy().and.returnValue(this.$q.when(rawUserNames))
        this.createController()
        this.controller.membership.userData = userNames.slice(1)
        this.controller.getUserNames()('')
          .then(reply => {
            expect(reply).toBeArrayOfSize(1)
            expect(reply[0].obj).toEqual(jasmine.any(this.UserName))
            expect(reply[0].obj).toEqual(userNames[0])
          })
        this.scope.$digest()
      })

    })

    it('membership is updated when a user is selected', function() {
      const entityInfo = this.entityInfoFrom(this.Factory.user())
      this.createController()
      expect(this.controller.membership.userData).toBeEmptyArray()
      this.controller.userSelected()(entityInfo)
      expect(this.controller.membership.userData).toBeArrayOfSize(1)
      expect(this.controller.membership.userData[0].id).toBe(entityInfo.id)
      expect(this.controller.membership.userData[0].name).toBe(entityInfo.name)
    })

    it('membership is updated when a user is removed', function() {
      const entityInfo = this.entityInfoFrom(this.Factory.user())
      this.createController()
      this.controller.membership.userData = [ entityInfo ]
      this.controller.removeUser()(entityInfo)
      expect(this.controller.membership.userData).toBeEmptyArray()
    })

  })

  describe('for studies', function() {

    describe('when getting studies', function() {

      it('gets study names', function() {
        const studyNames = [ this.StudyName.create(this.Factory.studyNameDto()) ]
        this.StudyName.list = jasmine.createSpy().and.returnValue(this.$q.when(studyNames))
        this.createController()
        this.controller.getStudyNames()('')
          .then(reply => {
            expect(reply).toBeArrayOfSize(studyNames.length)
            reply.forEach(item => {
              expect(item.obj).toEqual(jasmine.any(this.StudyName))
            })
          })
        this.scope.$digest()
      })

      it('omits study names if they are already selected', function() {
        const rawStudyNames = [ this.Factory.studyNameDto(),  this.Factory.studyNameDto() ],
              studyNames = rawStudyNames.map(this.StudyName.create)
        this.biobankApi.get = jasmine.createSpy().and.returnValue(this.$q.when(rawStudyNames))
        this.createController()
        this.controller.membership.studyData.entityData = studyNames.slice(1)
        this.controller.getStudyNames()('')
          .then(reply => {
            expect(reply).toBeArrayOfSize(1)
            expect(reply[0].obj).toEqual(jasmine.any(this.StudyName))
            expect(reply[0].obj).toEqual(studyNames[0])
          })
        this.scope.$digest()
      })

    })

    it('membership is updated when a study is selected', function() {
      const entityInfo = this.entityInfoFrom(this.Factory.study())
      this.createController()
      expect(this.controller.membership.studyData.entityData).toBeEmptyArray()
      this.controller.membership.centreData.allEntities = false
      this.controller.studySelected()(entityInfo)
      expect(this.controller.membership.studyData.entityData).toBeArrayOfSize(1)
      expect(this.controller.membership.studyData.entityData[0].id).toBe(entityInfo.id)
      expect(this.controller.membership.studyData.entityData[0].name).toBe(entityInfo.name)
    })

    it('membership is updated when a study is removed', function() {
      const entityInfo = this.entityInfoFrom(this.Factory.study())
      this.createController()
      this.controller.membership.studyData.entityData = [ entityInfo ]
      this.controller.removeStudy()(entityInfo)
      expect(this.controller.membership.studyData.entityData).toBeEmptyArray()
    })

    it('form is invalid if dirty and no centres and only study is removed', function() {
      const entityInfo = this.entityInfoFrom(this.Factory.study())
      this.createController()
      this.controller.membership.studyData.entityData = [ entityInfo ]
      this.controller.removeStudy()(entityInfo)
      expect(this.controller.$scope.membershipForm.$invalid).toBeTrue()
    })

    it('form is valid if no centres and membership is for all studies', function() {
      this.createController()
      this.controller.$scope.membershipForm.name.$setViewValue(this.Factory.stringNext())
      this.controller.membership.userData = this.entityInfoFrom(this.Factory.user())
      this.controller.membership.studyData.allEntities = true
      this.controller.setValidity()
      expect(this.controller.$scope.membershipForm.$valid).toBeTrue()
    })

    it('when all studies selected, form is valid', function() {
      this.createController()
      this.controller.$scope.membershipForm.name.$setViewValue(this.Factory.stringNext())
      this.controller.allStudiesMembership = true
      this.controller.allStudiesChanged();
      expect(this.controller.$scope.membershipForm.$valid).toBeTrue()
    })

    it('study entity data cleared when user selects all studies', function() {
      this.createController()
      const studyNames = [ this.StudyName.create(this.Factory.studyNameDto()) ]
      this.controller.membership.studyData.entityData = studyNames
      this.controller.allStudiesMembership = undefined
      this.controller.allStudiesChanged();
      expect(this.controller.membership.studyData.entityData).toBeEmptyArray()
    })

  })

  describe('for centres', function() {

    describe('when getting users', function() {

      it('gets centre names', function() {
        const centreNames = [ this.CentreName.create(this.Factory.centreNameDto()) ]
        this.CentreName.list = jasmine.createSpy().and.returnValue(this.$q.when(centreNames))
        this.createController()
        this.controller.getCentreNames()('')
          .then(reply => {
            expect(reply).toBeArrayOfSize(centreNames.length)
            reply.forEach(item => {
              expect(item.obj).toEqual(jasmine.any(this.CentreName))
            })
          })
        this.scope.$digest()
      })

      it('omits centre names if they are already selected', function() {
        const rawCentreNames = [ this.Factory.centreNameDto(), this.Factory.centreNameDto() ],
              centreNames    = rawCentreNames.map(this.CentreName.create)
        this.biobankApi.get = jasmine.createSpy().and.returnValue(this.$q.when(rawCentreNames))
        this.createController()
        this.controller.membership.centreData.entityData = centreNames.slice(1)
        this.controller.getCentreNames()('')
          .then(reply => {
            expect(reply).toBeArrayOfSize(1)
            expect(reply[0].obj).toEqual(jasmine.any(this.CentreName))
            expect(reply[0].obj).toEqual(centreNames[0])
          })
        this.scope.$digest()
      })

    })
    it('membership is updated when a centre is selected', function() {
      const entityInfo = this.entityInfoFrom(this.Factory.centre())
      this.createController()
      this.controller.membership.studyData.allEntities = false
      expect(this.controller.membership.centreData.entityData).toBeEmptyArray()
      this.controller.centreSelected()(entityInfo)
      expect(this.controller.membership.centreData.entityData).toBeArrayOfSize(1)
      expect(this.controller.membership.centreData.entityData[0].id).toBe(entityInfo.id)
      expect(this.controller.membership.centreData.entityData[0].name).toBe(entityInfo.name)
    })

    it('membership is updated when a centre is removed', function() {
      const entityInfo = this.entityInfoFrom(this.Factory.centre())
      this.createController()
      this.controller.membership.centreData.entityData = [ entityInfo ]
      this.controller.removeCentre()(entityInfo)
      expect(this.controller.membership.centreData.entityData).toBeEmptyArray()
    })

    it('form is invalid if dirty and no studies and only centre is removed', function() {
      const entityInfo = this.entityInfoFrom(this.Factory.centre())
      this.createController()
      this.controller.membership.centreData.entityData = [ entityInfo ]
      this.controller.removeCentre()(entityInfo)
      expect(this.controller.$scope.membershipForm.$invalid).toBeTrue()
    })

    it('form is valid if no centres and membership is for all studies', function() {
      this.createController()
      this.controller.$scope.membershipForm.name.$setViewValue(this.Factory.stringNext())
      this.controller.membership.userData = this.entityInfoFrom(this.Factory.user())
      this.controller.membership.centreData.allEntities = true
      this.controller.setValidity()
      expect(this.controller.$scope.membershipForm.$valid).toBeTrue()
    })

    it('when all centres selected, form is valid', function() {
      this.createController()
      this.controller.$scope.membershipForm.name.$setViewValue(this.Factory.stringNext())
      this.controller.allCentresMembership = true
      this.controller.allCentresChanged();
      expect(this.controller.$scope.membershipForm.$valid).toBeTrue()
    })

    it('centre entity data cleared when user selects all centres', function() {
      this.createController()
      const centreNames = [ this.CentreName.create(this.Factory.centreNameDto()) ]
      this.controller.membership.centreData.entityData = centreNames
      this.controller.allCentresMembership = undefined
      this.controller.allCentresChanged();
      expect(this.controller.membership.centreData.entityData).toBeEmptyArray()
    })

  })

  describe('for form submission', function() {

    it('transitions to correct state on valid form submission', function() {
      const membership = this.Membership.create(this.Factory.membership())
      this.notificationsService.submitSuccess = jasmine.createSpy().and.returnValue(null)
      this.$state.go = jasmine.createSpy().and.returnValue(null)
      this.Membership.prototype.add = jasmine.createSpy().and.returnValue(this.$q.when(membership))
      this.createController()
      this.controller.submit()
      this.scope.$digest()
      expect(this.notificationsService.submitSuccess).toHaveBeenCalled()
      expect(this.$state.go).toHaveBeenCalledWith('home.admin.access.memberships', {}, { reload: true })
    })

    it('handles rejection of membership update', function() {
      this.domainNotificationService.updateErrorModal = jasmine.createSpy().and.returnValue(null)
      this.createController()
      this.Membership.prototype.add = jasmine.createSpy().and.returnValue(this.$q.reject('simulated error'))
      this.controller.submit()
      this.scope.$digest()
      expect(this.domainNotificationService.updateErrorModal).toHaveBeenCalled()
    })

  })

  it('transitions to correct state on when cancel button is pressed', function() {
    this.$state.go = jasmine.createSpy().and.returnValue(null)
    this.createController()
    this.controller.cancel()
    this.scope.$digest()
    expect(this.$state.go).toHaveBeenCalledWith('home.admin.access.memberships')
  })

})
