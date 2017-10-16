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

    // taken from here:
    // https://stackoverflow.com/questions/22436501/simple-angularjs-form-is-undefined-in-scope
    this.$scope.$watch('membershipForm', () => {
      this.$scope.membershipForm.$setValidity('studyOrCentreRequired', false)
    })
  }

  getUserNames(viewValue) {
    var omitUserNames = this.membership.userData
        .map(entityInfo =>  this.UserName.create(_.pick(entityInfo, ['id', 'name'])))
    return this.UserName.list({ filter: 'name:like:' + viewValue}, omitUserNames)
      .then(nameObjs =>
            nameObjs.map((nameObj) => ({
              label: nameObj.name,
              obj: nameObj
            })))
  }

  userSelected(selection) {
    this.membership.userData.push(selection)
  }

  removeUser(userTag) {
    _.remove(this.membership.userData,
             userData =>  userData.name === userTag.name)
  }

  getStudyNames(viewValue) {
    var omitStudyNames = this.membership.studyData.entityData
        .map(entityInfo =>  this.StudyName.create(_.pick(entityInfo, ['id', 'name'])))
    return this.StudyName.list({ filter: 'name:like:' + viewValue}, omitStudyNames)
      .then(names =>
            names.map(name => ({
              label: name.name,
              obj: new this.EntityInfo({ id: name.id, name: name.name })
            })))
  }

  studySelected(selection) {
    this.membership.studyData.entityData.push(selection)
    this.$scope.membershipForm.$setValidity('studyOrCentreRequired', true)
  }

  removeStudy(studyTag) {
    _.remove(this.membership.studyData.entityData,
             studyData => studyData.name === studyTag.name)
    this.setValidity()
  }

  getCentreNames(viewValue) {
    var omitCentreNames = this.membership.centreData.entityData
        .map(entityInfo => this.CentreName.create(_.pick(entityInfo, ['id', 'name'])))

    return this.CentreName.list({ filter: 'name:like:' + viewValue}, omitCentreNames)
      .then(names =>
            names.map(name => ({
              label: name.name,
              obj:   new this.EntityInfo({ id: name.id, name: name.name })
            })))
  }

  centreSelected(selection) {
    this.membership.centreData.entityData.push(selection)
    this.$scope.membershipForm.$setValidity('studyOrCentreRequired', true)
  }

  removeCentre(centreTag) {
    _.remove(this.membership.centreData.entityData,
             centreData => centreData.name === centreTag.name)
    this.setValidity()
  }

  setValidity() {
    if (this.membership.studyData.allEntities || this.membership.centreData.allEntities) { return }

    if ((this.membership.studyData.entityData.length <= 0) &&
        (this.membership.centreData.entityData.length <= 0)) {
      this.$scope.membershipForm.$setValidity('studyOrCentreRequired', false)
    }
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

}

const COMPONENT = {
  template: require('./membershipAdd.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
  }
}

export default COMPONENT
