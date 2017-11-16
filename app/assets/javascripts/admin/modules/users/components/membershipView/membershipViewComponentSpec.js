/**
 * Jasmine test suite
 *
 */
/* global angular */

import ngModule from '../../index'

describe('membershipViewComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test')
    angular.mock.inject(function(ComponentTestSuiteMixin, MembershipSpecCommon) {
      Object.assign(this, ComponentTestSuiteMixin, MembershipSpecCommon)

      this.injectDependencies('$q',
                              '$state',
                              'Membership',
                              'User',
                              'UserName',
                              'StudyName',
                              'CentreName',
                              'UserState',
                              'userService',
                              'domainNotificationService',
                              'modalService',
                              'asyncInputModal',
                              'notificationsService',
                              'modalInput',
                              'Factory')

      this.createController = (membership) => {
        ComponentTestSuiteMixin.createController.call(
          this,
          '<membership-view membership="vm.membership"></membership-view>',
          { membership },
          'membershipView')
      }

      this.createCurrentUserSpy = () => {
        const user = this.User.create(this.Factory.user())
        this.userService.getCurrentUser = jasmine.createSpy().and.returnValue(user)
      }
    })
  })

  beforeEach(function() {
    this.createCurrentUserSpy()
  })

  it('has valid scope', function() {
    this.createController(this.Membership.create(this.Factory.membership()))
    expect(this.controller.userCanUpdate).toBeDefined()
    expect(this.controller.breadcrumbs).toBeDefined()
    expect(this.controller.noStudiesMembership).toBeDefined()
    expect(this.controller.noCentresMembership).toBeDefined()
    expect(this.controller.userNameLabels).toBeArray()
    expect(this.controller.studyNameLabels).toBeArray()
    expect(this.controller.centreNameLabels).toBeArray()
  })

  it('has valid user labels', function() {
    const userName = this.UserName.create(this.Factory.userNameDto())
    const membership = this.Membership.create(this.Factory.membership({ userData: [ userName ] }))
    this.createController(membership)
    expect(this.controller.userNameLabels).toBeArrayOfSize(1)
    compareLabelInfoToEntityName(this.controller.userNameLabels[0], userName)
  })

  it('has valid study labels', function() {
    const studyName = this.StudyName.create(this.Factory.studyNameDto())
    const membership = this.Membership.create(this.Factory.membership({
      studyData: {
        allEntities: false,
        entityData: [ studyName ]
      }
    }))
    this.createController(membership)
    expect(this.controller.studyNameLabels).toBeArrayOfSize(1)
    compareLabelInfoToEntityName(this.controller.studyNameLabels[0], studyName)
  })

  it('has valid centre labels', function() {
    const centreName = this.CentreName.create(this.Factory.centreNameDto())
    const membership = this.Membership.create(this.Factory.membership({
      centreData: {
        allEntities: false,
        entityData: [ centreName ]
      }
    }))
    this.createController(membership)
    expect(this.controller.centreNameLabels).toBeArrayOfSize(1)
    compareLabelInfoToEntityName(this.controller.centreNameLabels[0], centreName)
  })

  describe('when removing a membership', function() {

    beforeEach(function() {
      this.modalService.modalOkCancel   = jasmine.createSpy().and.returnValue(this.$q.when('OK'))
      this.notificationsService.success = jasmine.createSpy().and.returnValue(this.$q.when(null))
      this.createController(this.Membership.create(this.Factory.membership()))
    })

    it('can remove a membership', function() {
      this.Membership.prototype.remove  = jasmine.createSpy().and.returnValue(this.$q.when(true))
      this.controller.remove()
      this.scope.$digest()
      expect(this.Membership.prototype.remove).toHaveBeenCalled()
      expect(this.notificationsService.success).toHaveBeenCalled()
      expect(this.modalService.modalOkCancel.calls.count()).toBe(1)
    })

    it('user is informed if a membership removal attempt fails', function() {
      this.Membership.prototype.remove = jasmine.createSpy().and.returnValue(this.$q.reject('simulated error'))
      this.controller.remove()
      this.scope.$digest()
      expect(this.Membership.prototype.remove).toHaveBeenCalled()
      expect(this.notificationsService.success).not.toHaveBeenCalled()
      expect(this.modalService.modalOkCancel.calls.count()).toBe(2)
    })

  })

  describe('updating name', function () {

    const context = {};

    beforeEach(function () {
      context.controllerUpdateFuncName = 'editName'
      context.modalInputFuncName       = 'text'
      context.membershipUpdateFuncName = 'updateName'
      context.newValue                 = this.Factory.stringNext()
    });

    sharedUpdateBehaviour(context);

  });

  describe('updating description', function () {

    const context = {};

    beforeEach(function () {
      context.controllerUpdateFuncName = 'editDescription'
      context.modalInputFuncName       = 'textArea'
      context.membershipUpdateFuncName = 'updateDescription'
      context.newValue                 = this.Factory.stringNext()
    });

    sharedUpdateBehaviour(context);

  });

  describe('adding users', function() {
    const context = {};

    beforeEach(function () {
      const userName = this.UserName.create(this.Factory.userNameDto()),
            rawMembership = this.Factory.membership()

      context.controllerAddEntityFuncName     = 'addUser'
      context.addEntityFuncName               = 'addUser'
      context.entityName                      = userName
      context.membership                      = this.Membership.create(rawMembership)
      context.controllerEntityLabelsFieldName = 'userNameLabels'
      context.entityNameClass                 = this.UserName
      context.controllerGetMatchingEntityNamesFuncName = 'getMatchingUserNames'

      context.replyMembership =
        this.Membership.create(Object.assign({}, rawMembership, { userData: [ userName ]}))
    });

    sharedAsyncModalBehaviour(context);

  })

  describe('adding studies', function() {
    const context = {};

    beforeEach(function () {
      const studyName = this.StudyName.create(this.Factory.studyNameDto()),
            rawMembership = this.Factory.membership()

      context.controllerAddEntityFuncName     = 'addStudy'
      context.addEntityFuncName               = 'addStudy'
      context.entityName                      = studyName
      context.membership                      = this.Membership.create(rawMembership)
      context.controllerEntityLabelsFieldName = 'studyNameLabels'
      context.entityNameClass                 = this.StudyName
      context.controllerGetMatchingEntityNamesFuncName = 'getMatchingStudyNames'

      context.replyMembership = this.Membership.create(Object.assign({}, rawMembership, {
          studyData: {
            allEntities: false,
            entityData: [ studyName ]
          }}))
    });

    sharedAsyncModalBehaviour(context);

  })

  describe('adding centres', function() {
    const context = {};

    beforeEach(function () {
      const centreName = this.CentreName.create(this.Factory.centreNameDto()),
            rawMembership = this.Factory.membership()

      context.controllerAddEntityFuncName     = 'addCentre'
      context.addEntityFuncName               = 'addCentre'
      context.entityName                      = centreName
      context.membership                      = this.Membership.create(rawMembership)
      context.controllerEntityLabelsFieldName = 'centreNameLabels'
      context.entityNameClass                 = this.CentreName
      context.controllerGetMatchingEntityNamesFuncName = 'getMatchingCentreNames'

      context.replyMembership = this.Membership.create(Object.assign({}, rawMembership, {
          centreData: {
            allEntities: false,
            entityData: [ centreName ]
          }}))
    });

    sharedAsyncModalBehaviour(context);

  })

  describe('selecting a user label tag', function() {
    const context = {};

    beforeEach(function () {
      context.entityName = this.UserName.create(this.Factory.userNameDto())
      context.labelSelectedFuncName = 'userLabelSelected'
      context.membershipRemoveFuncName = 'removeUser'
    });

    sharedTagSelectedBehaviour(context);

  })

  describe('selecting a study label tag', function() {
    const context = {};

    beforeEach(function () {
      context.entityName = this.StudyName.create(this.Factory.studyNameDto())
      context.labelSelectedFuncName = 'studyLabelSelected'
      context.membershipRemoveFuncName = 'removeStudy'
    });

    sharedTagSelectedBehaviour(context);

  })

  describe('selecting a centre label tag', function() {
    const context = {};

    beforeEach(function () {
      context.entityName = this.CentreName.create(this.Factory.centreNameDto())
      context.labelSelectedFuncName = 'centreLabelSelected'
      context.membershipRemoveFuncName = 'removeCentre'
    });

    sharedTagSelectedBehaviour(context);

  })

  it('pressing back button returns to correct state', function() {
    this.$state.go = jasmine.createSpy().and.returnValue(null);
    this.createController(this.Membership.create(this.Factory.membership()))
    this.controller.back();
    this.scope.$digest();
    expect(this.$state.go).toHaveBeenCalledWith('home.admin.users.memberships');
  });

  function sharedUpdateBehaviour(context) {

    describe('(shared)', function() {

      beforeEach(function() {
        this.membership = this.Membership.create(this.Factory.membership())
        this.modalInput[context.modalInputFuncName] =
          jasmine.createSpy().and.returnValue({ result: this.$q.when(context.newValue)})
        this.notificationsService.updateError = jasmine.createSpy().and.returnValue(this.$q.when('OK'))
        this.notificationsService.success = jasmine.createSpy().and.returnValue(this.$q.when('OK'))
      });

      it('on update should invoke the update method on entity', function() {
        this.Membership.prototype[context.membershipUpdateFuncName] =
          jasmine.createSpy().and.returnValue(this.$q.when(this.membership))

        this.createController(this.membership)
        this.controller[context.controllerUpdateFuncName]()
        this.scope.$digest()
        expect(this.Membership.prototype[context.membershipUpdateFuncName]).toHaveBeenCalled()
        expect(this.notificationsService.success).toHaveBeenCalled()
      })

      it('error message should be displayed when update fails', function() {
        this.createController(this.membership)
        this.Membership.prototype[context.membershipUpdateFuncName] =
          jasmine.createSpy().and.returnValue(this.$q.reject('simulated error'))
        this.controller[context.controllerUpdateFuncName]()
        this.scope.$digest()
        expect(this.notificationsService.updateError).toHaveBeenCalled()
      })

    })
  }

  function sharedAsyncModalBehaviour(context) {

    describe('(shared)', function() {

      it('can add a user', function() {
        this.Membership.prototype[context.addEntityFuncName] =
          jasmine.createSpy().and.returnValue(this.$q.when(context.replyMembership))
        this.asyncInputModal.open =
          jasmine.createSpy().and.returnValue({
            result: this.$q.when(entityNameToAsyncModalResult(context.entityName))
          })

        this.createController(context.membership)
        expect(this.controller[context.controllerEntityLabelsFieldName]).toBeArrayOfSize(0)
        this.controller[context.controllerAddEntityFuncName]()
        this.scope.$digest()
        expect(this.controller[context.controllerEntityLabelsFieldName]).toBeArrayOfSize(1)
        compareLabelInfoToEntityName(this.controller[context.controllerEntityLabelsFieldName][0],
                                     context.entityName)
      })

      it('retrieves matching entity names', function() {
        this.createController(context.membership)
        context.entityNameClass.list =
          jasmine.createSpy().and.returnValue(this.$q.when([ context.entityName ]))
        this.controller[context.controllerGetMatchingEntityNamesFuncName]()()
          .then(nameObjs => {
            expect(nameObjs).toBeArrayOfSize(1)
            expect(nameObjs[0].label).toBe(context.entityName.name)
            expect(nameObjs[0].obj).toBe(context.entityName)
          });
        this.scope.$digest()
        expect(context.entityNameClass.list).toHaveBeenCalled()
      })

      it('entity labels not modified is user presses the modal cancel button ', function() {
        this.createController(context.membership)
        this.asyncInputModal.open =
          jasmine.createSpy().and.returnValue({ result: this.$q.reject('cancel pressed') })
        expect(this.controller[context.controllerEntityLabelsFieldName]).toBeArrayOfSize(0)
        this.controller[context.controllerAddEntityFuncName]()
        expect(this.controller[context.controllerEntityLabelsFieldName]).toBeArrayOfSize(0)
      })

    })

  }

  function sharedTagSelectedBehaviour(context) {

    describe('(shared)', function() {

      beforeEach(function() {
        this.membership = this.Membership.create(this.Factory.membership())
        this.modalService.modalOkCancel   = jasmine.createSpy().and.returnValue(this.$q.when('OK'))
        this.notificationsService.success = jasmine.createSpy().and.returnValue(this.$q.when(null))
        this.createController(this.membership)
      })

      it('can remove the entity associated with the tag', function() {
        this.Membership.prototype[context.membershipRemoveFuncName] =
          jasmine.createSpy().and.returnValue(this.$q.when(this.membership))
        this.controller[context.labelSelectedFuncName]()(context.entityName)
        this.scope.$digest()
        expect(this.Membership.prototype[context.membershipRemoveFuncName]).toHaveBeenCalled()
        expect(this.notificationsService.success).toHaveBeenCalled()
        expect(this.modalService.modalOkCancel.calls.count()).toBe(1)
      })

      it('user is informed if removal of the entity attempt fails', function() {
        this.Membership.prototype[context.membershipRemoveFuncName] =
          jasmine.createSpy().and.returnValue(this.$q.reject('simulated error'))
        this.controller[context.labelSelectedFuncName]()(context.entityName)
        this.scope.$digest()
        expect(this.Membership.prototype[context.membershipRemoveFuncName]).toHaveBeenCalled()
        expect(this.notificationsService.success).not.toHaveBeenCalled()
        expect(this.modalService.modalOkCancel.calls.count()).toBe(2)
      })

      it('user can press cancel on the verification modal', function() {
        this.Membership.prototype[context.membershipRemoveFuncName] =
          jasmine.createSpy().and.returnValue(this.$q.when(this.membership))
        this.modalService.modalOkCancel   = jasmine.createSpy().and.returnValue(this.$q.reject('Cancel'))
        this.controller[context.labelSelectedFuncName]()(context.entityName)
        this.scope.$digest()
        expect(this.Membership.prototype[context.membershipRemoveFuncName]).not.toHaveBeenCalled()
        expect(this.notificationsService.success).not.toHaveBeenCalled()
        expect(this.modalService.modalOkCancel.calls.count()).toBe(1)
      })

    })

  }

  function compareLabelInfoToEntityName(labelInfo, entityName) {
    expect(labelInfo.label).toBe(entityName.name)
    expect(labelInfo.tooltip).toContain('Remove')
    expect(labelInfo.tooltip).toContain(entityName.name)
    expect(labelInfo.obj.id).toBe(entityName.id)
    expect(labelInfo.obj.name).toBe(entityName.name)
  }

  function entityNameToAsyncModalResult(entityName) {
    return {
      label: entityName.name,
      obj: entityName
    }
  }

})
