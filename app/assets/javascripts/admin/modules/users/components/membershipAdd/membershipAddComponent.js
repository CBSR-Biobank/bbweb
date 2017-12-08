/**
 * This component allows a user to add a new {@link domain.users.Membership|Membership} to the system.
 */

import _ from 'lodash'

/*
 *
 */
class Controller {

  constructor($scope,
              $state,
              notificationsService,
              domainNotificationService,
              Membership,
              breadcrumbService,
              gettextCatalog,
              EntityInfo,
              UserName,
              StudyName,
              CentreName) {
    'ngInject'
    Object.assign(this, {
      $scope,
      $state,
      notificationsService,
      domainNotificationService,
      Membership,
      breadcrumbService,
      gettextCatalog,
      EntityInfo,
      UserName,
      StudyName,
      CentreName
    })
    this.returnState = 'home.admin.users.memberships'
  }

  $onInit() {
    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.users'),
      this.breadcrumbService.forState('home.admin.users.memberships'),
      this.breadcrumbService.forState('home.admin.users.memberships.add')
    ]

    this.membership = new this.Membership()
    this.membership.studyData.allEntities = true
    this.membership.centreData.allEntities = true
    this.allStudiesMembership = true;
    this.allCentresMembership = true;

    // taken from here:
    // https://stackoverflow.com/questions/22436501/simple-angularjs-form-is-undefined-in-scope
    this.$scope.$watch('membershipForm', () => {
      this.$scope.membershipForm.$setValidity('studyOrCentreRequired', true)
    })
  }

  getUserNames() {
    return (viewValue) => {
      const omitUserNames = this.membership.userData.map(info => this.entityInfoToUserName(info))
      return this.UserName.list({ filter: 'name:like:' + viewValue}, omitUserNames)
        .then(nameObjs => nameObjs.map(nameObj => ({
          label: nameObj.name,
          obj: nameObj
        })))
    }
  }

  userSelected() {
    return (selection) => {
      this.membership.userData.push(selection)
    }
  }

  removeUser() {
    return (userTag) => {
      _.remove(this.membership.userData,
               userData =>  userData.name === userTag.name)
    }
  }

  getStudyNames() {
    return (viewValue) => {
      const omitStudyNames = this.membership.studyData.entityData
            .map(info =>  this.entityInfoToStudyName(info))
      return this.StudyName.list({ filter: 'name:like:' + viewValue}, omitStudyNames)
        .then(namesObjs =>
              namesObjs.map(nameObj => ({
                label: nameObj.name,
                obj:   nameObj
              })))
    }
  }

  studySelected() {
    return (selection) => {
      this.membership.studyData.allEntities = false
      this.membership.studyData.entityData.push(selection)
      this.setValidity()
    }
  }

  removeStudy() {
    return (studyTag) => {
      _.remove(this.membership.studyData.entityData,
               studyData => studyData.name === studyTag.name)
      this.setValidity()
    }
  }

  allStudiesChanged() {
    this.membership.studyData.allEntities = this.allStudiesMembership !== undefined
    if (!this.membership.studyData.allEntities) {
      this.membership.studyData.entityData = []
    }
    this.setValidity()
  }

  getCentreNames() {
    return (viewValue) => {
      const omitCentreNames = this.membership.centreData.entityData
            .map(info => this.entityInfoToCentreName(info))

      return this.CentreName.list({ filter: 'name:like:' + viewValue}, omitCentreNames)
        .then(nameObjs =>
              nameObjs.map(nameObj => ({
                label: nameObj.name,
                obj:   nameObj
              })))
    }
  }

  centreSelected() {
    return (selection) => {
      this.membership.centreData.allEntities = false
      this.membership.centreData.entityData.push(selection)
      this.setValidity()
    }
  }

  removeCentre() {
    return (centreTag) => {
      _.remove(this.membership.centreData.entityData,
               centreData => centreData.name === centreTag.name)
      this.setValidity()
    }
  }

  allCentresChanged() {
    this.membership.centreData.allEntities = this.allCentresMembership !== undefined
    if (!this.membership.centreData.allEntities) {
      this.membership.centreData.entityData = []
    }
    this.setValidity()
  }

  setValidity() {
    if (this.membership.studyData.allEntities || this.membership.centreData.allEntities) {
      this.$scope.membershipForm.$setValidity('studyOrCentreRequired', true)
      return
    }

    const valid = ((this.membership.studyData.entityData.length > 0) ||
                   (this.membership.centreData.entityData.length > 0))

    this.$scope.membershipForm.$setValidity('studyOrCentreRequired', valid)
  }

  submit() {
    this.membership.add()
      .then(() => {
        this.notificationsService.submitSuccess()
        this.$state.go(this.returnState, {}, { reload: true })
      })
      .catch((error) => {
        this.domainNotificationService.updateErrorModal(error, this.gettextCatalog.getString('centre'))
      })
  }

  cancel() {
    this.$state.go(this.returnState)
  }

  entityInfoToUserName(name)  {
    return this.UserName.create(Object.assign(_.pick(name, 'id', 'name'), { state: '' }))
  }

  entityInfoToStudyName(name)  {
    return this.StudyName.create(Object.assign(_.pick(name, 'id', 'name'), { state: '' }))
  }

  entityInfoToCentreName(name)  {
    return this.CentreName.create(Object.assign(_.pick(name, 'id', 'name'), { state: '' }))
  }

}

const COMPONENT = {
  template: require('./membershipAdd.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
  }
}

export default ngModule => ngModule.component('membershipAdd', COMPONENT)
