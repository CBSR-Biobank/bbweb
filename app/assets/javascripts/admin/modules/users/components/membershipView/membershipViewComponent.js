/**
 * AngularJS Component for {@link domain.users.User User} administration.
 *
 * @namespace admin.users.components.membershipsView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';
import angular from 'angular';

class MembershipViewController {

  constructor($q,
              $log,
              $state,
              notificationsService,
              domainNotificationService,
              gettextCatalog,
              breadcrumbService,
              userService,
              modalInput,
              asyncInputModal,
              EntityInfo,
              UserName,
              StudyName,
              CentreName,
              matchingUserNames,
              modalService,
              MembershipChangeStudiesModal,
              MembershipChangeCentresModal) {
    'ngInject';

    Object.assign(this,
                  {
                    $q,
                    $log,
                    $state,
                    notificationsService,
                    domainNotificationService,
                    gettextCatalog,
                    breadcrumbService,
                    userService,
                    modalInput,
                    asyncInputModal,
                    EntityInfo,
                    UserName,
                    StudyName,
                    CentreName,
                    matchingUserNames,
                    modalService,
                    MembershipChangeStudiesModal,
                    MembershipChangeCentresModal
                  });
  }

  $onInit() {
    this.userCanUpdate = this.userService.getCurrentUser().hasUserAdminRole();

    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.access'),
      this.breadcrumbService.forState('home.admin.access.memberships'),
      this.breadcrumbService.forStateWithFunc(
        'home.admin.access.memberships.membership',
        () => this.membership.name)
    ];

    this.noStudiesMembership = (!this.membership.studyData.allEntities &&
                                (this.membership.studyData.entityData.length <= 0));

    this.noCentresMembership = (!this.membership.centreData.allEntities &&
                                (this.membership.centreData.entityData.length <= 0));

    this.userNameLabels   = this.entityNamesToLabels(this.membership.userData);
    this.studyNameLabels  = this.entityNamesToLabels(this.membership.studyData.entityData);
    this.centreNameLabels = this.entityNamesToLabels(this.membership.centreData.entityData);
  }

  entityNamesToLabels(entityData) {
    var labels = entityData.map((userInfo) => ({
      label:   userInfo.name,
      tooltip: this.gettextCatalog.getString('Remove ' + userInfo.name),
      obj:     userInfo
    }));
    return _.sortBy(labels, [ 'label' ]);
  }

  remove() {
    const promiseFn =
          () => this.membership.remove()
          .then(() => {
            this.notificationsService.success(this.gettextCatalog.getString('Membership removed'));
            this.$state.go('home.admin.access.memberships', {}, { reload: true });
          })

    this.domainNotificationService.removeEntity(
      promiseFn,
      this.gettextCatalog.getString('Remove membership'),
      this.gettextCatalog.getString('Are you sure you want to remove the membership named <b>{{name}}</b>?',
                                    { name: this.membership.name }),
      this.gettextCatalog.getString('Remove failed'),
      this.gettextCatalog.getString('Membership with name {{name}} cannot be removed',
                                    { name: this.membership.name }))
      .catch(angular.noop);
  }

  postUpdate(message, title, timeout = 1500) {
    return (membership) => {
      this.membership = membership;
      this.notificationsService.success(message, title, timeout);
    };
  }

  editName() {
    this.modalInput.text(this.gettextCatalog.getString('Membership name'),
                         this.gettextCatalog.getString('Name'),
                         this.membership.name,
                         { required: true, minLength: 2 }).result
      .then((name) => {
        this.membership.updateName(name)
          .then(this.postUpdate(this.gettextCatalog.getString('Name changed successfully.'),
                                this.gettextCatalog.getString('Change successful')))
          .catch(this.notificationsService.updateError);
      })
      .catch((error) => {
        this.$log.error(error);
      });
  }

  editDescription() {
    this.modalInput.textArea(this.gettextCatalog.getString('Membership description'),
                             this.gettextCatalog.getString('Description'),
                             this.membership.description).result
      .then((description) => {
        this.membership.updateDescription(description)
          .then(this.postUpdate(this.gettextCatalog.getString('Description changed successfully.'),
                                this.gettextCatalog.getString('Change successful')))
          .catch(this.notificationsService.updateError);
      })
      .catch((error) => {
        this.$log.error(error);
      });
  }

  addUser() {
    this.matchingUserNames.open(this.gettextCatalog.getString('Add user to membership'),
                                this.gettextCatalog.getString('User'),
                                this.gettextCatalog.getString('enter a user\'s name or partial name'),
                                this.gettextCatalog.getString('No matching users found'),
                                [])
      .then(modalValue => this.membership.addUser(modalValue.obj.id))
      .then((membership) => {
        this.membership = membership;
        this.userNameLabels = this.entityNamesToLabels(this.membership.userData);
      })
      .catch((error) => {
        this.$log.error(error);
      });
  }

  // this method is invoked by a child component, so a callback function is returned
  // called to remove a user from the membership
  userLabelSelected(userName) {
    const promiseFn = () =>
          this.membership.removeUser(userName.id).then((membership) => {
            this.notificationsService.success(this.gettextCatalog.getString(
              'User {{name}} removed',
              { name: userName.name }));
            this.updateMembership(membership);
          });

    this.domainNotificationService.removeEntity(
      promiseFn,
      this.gettextCatalog.getString('Remove user from membership'),
      this.gettextCatalog.getString(
        'Are you sure you want to remove the user named <strong>{{name}}</strong> from this membership?',
        { name: userName.name }),
      this.gettextCatalog.getString('Remove failed'),
      this.gettextCatalog.getString(
        'User named {{name}} cannot be removed',
        { name: userName.name }))
      .catch(angular.noop);
  }

  changeStudies() {
    this.MembershipChangeStudiesModal.open(this.membership)
      .result
      .then(modalValue => this.membership.updateStudyData(modalValue))
      .then((membership) => {
        this.membership = membership;
        this.studyNameLabels = this.entityNamesToLabels(this.membership.studyData.entityData);
        this.notificationsService.success(this.gettextCatalog.getString('Studies updated'));
      })
      .catch(error => {
        this.domainNotificationService
          .updateErrorModal(error, this.gettextCatalog.getString('membership'));
        return null;
      });
  }

  // this method is invoked by a child component, so a callback function is returned
  // called to remove a user from the membership
  studyLabelSelected(studyName) {
    const promiseFn = () =>
          this.membership.removeStudy(studyName.id).then((membership) => {
            this.notificationsService.success(this.gettextCatalog.getString(
              'Study {{name}} removed',
              { name: studyName.name }));
            this.updateMembership(membership);
          });

    this.domainNotificationService.removeEntity(
      promiseFn,
      this.gettextCatalog.getString('Remove study from membership'),
      this.gettextCatalog.getString(
        'Are you sure you want to remove the study named <strong>{{name}}</strong> from this membership?',
        { name: studyName.name }),
      this.gettextCatalog.getString('Remove failed'),
      this.gettextCatalog.getString(
        'Study named {{name}} cannot be removed',
        { name: studyName.name }))
      .catch(angular.noop);
  }

  changeCentres() {
    this.MembershipChangeCentresModal.open(this.membership)
      .result
      .then(modalValue => this.membership.updateCentreData(modalValue))
      .then((membership) => {
        this.membership = membership;
        this.centreNameLabels = this.entityNamesToLabels(this.membership.centreData.entityData);
        this.notificationsService.success(this.gettextCatalog.getString('Centres updated'));
      })
       .catch(error => {
        this.domainNotificationService
          .updateErrorModal(error, this.gettextCatalog.getString('membership'));
        return null;
      });
  }

  // this method is invoked by a child component, so a callback function is returned
  // called to remove a user from the membership
  centreLabelSelected(centreName) {
    const promiseFn = () =>
          this.membership.removeCentre(centreName.id).then((membership) => {
            this.notificationsService.success(this.gettextCatalog.getString(
              'Centre {{name}} removed',
              { name: centreName.name }));
            this.updateMembership(membership);
          });

    this.domainNotificationService.removeEntity(
      promiseFn,
      this.gettextCatalog.getString('Remove centre from membership'),
      this.gettextCatalog.getString(
        'Are you sure you want to remove the centre named <strong>{{name}}</strong> from this membership?',
        { name: centreName.name }),
      this.gettextCatalog.getString('Remove failed'),
      this.gettextCatalog.getString(
        'Centre named {{name}} cannot be removed',
        { name: centreName.name }))
      .catch(angular.noop);
  }

  back() {
    this.$state.go('home.admin.access.memberships');
  }

  updateMembership(membership) {
    this.membership       = membership;
    this.userNameLabels   = this.entityNamesToLabels(this.membership.userData);
    this.studyNameLabels  = this.entityNamesToLabels(this.membership.studyData.entityData);
    this.centreNameLabels = this.entityNamesToLabels(this.membership.centreData.entityData);
  }

}

/**
 * An AngularJS component that allows the user to view the configuration for a {@link domain.access.Membership
 * Membership}.
 *
 * The user is also allowed to modify or delete this membership.
 *
 * @memberOf admin.users.components.membershipsView
 *
 * @param {domain.access.Membership} membership - the membership to view.
 */
const membershipViewComponent = {
  template: require('./membershipView.html'),
  controller: MembershipViewController,
  controllerAs: 'vm',
  bindings: {
    membership: '<'
  }
};

export default ngModule => ngModule.component('membershipView', membershipViewComponent)
