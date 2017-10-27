/**
 *
 */
import _       from 'lodash';
import angular from 'angular';

class MembershipViewController {

  constructor($state,
              notificationsService,
              domainNotificationService,
              gettextCatalog,
              breadcrumbService,
              usersService,
              modalInput,
              asyncInputModal,
              EntityInfo,
              UserName,
              StudyName,
              CentreName) {
    'ngInject';

    Object.assign(this,
                  {
                    $state,
                    notificationsService,
                    domainNotificationService,
                    gettextCatalog,
                    breadcrumbService,
                    usersService,
                    modalInput,
                    asyncInputModal,
                    EntityInfo,
                    UserName,
                    StudyName,
                    CentreName
                  });

    this.onUserLabelSelected   = this.userLabelSelected.bind(this);
    this.onStudyLabelSelected  = this.studyLabelSelected.bind(this);
    this.onCentreLabelSelected = this.centreLabelSelected.bind(this);
  }

  $onInit() {
    this.userCanUpdate = this.usersService.getCurrentUser().hasRole('UserAdministrator');

    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.users'),
      this.breadcrumbService.forState('home.admin.users.memberships'),
      this.breadcrumbService.forStateWithFunc(
        'home.admin.users.memberships.membership',
        () => this.membership.name)
    ];

    this.noStudiesMembership = (!this.membership.studyData.allEntities &&
                                (this.membership.studyData.entityData.length <= 0));

    this.noCentresMembership = (!this.membership.centreData.allEntities &&
                                (this.membership.centreData.entityData.length <= 0));

    this.userNameLabels   = this.userNamesToLabels();
    this.studyNameLabels  = this.studyNamesToLabels();
    this.centreNameLabels = this.centreNamesToLabels();
  }

  entityNamesToLabels(entityData) {
    var labels = entityData.map((userInfo) => ({
      label:   userInfo.name,
      tooltip: this.gettextCatalog.getString('Remove ' + userInfo.name),
      obj:     userInfo
    }));
    return _.sortBy(labels, [ 'label' ]);
  }

  userNamesToLabels() {
    return this.entityNamesToLabels(this.membership.userData);
  }

  studyNamesToLabels() {
    return this.entityNamesToLabels(this.membership.studyData.entityData);
  }

  centreNamesToLabels() {
    return this.entityNamesToLabels(this.membership.centreData.entityData);
  }

  remove() {
    const promiseFn =
          () => this.membership.remove()
          .then(() => {
            this.notificationsService.success(this.gettextCatalog.getString('Membership removed'));
            this.$state.go('home.admin.users.memberships', {}, { reload: true });
          })

    this.domainNotificationService.removeEntity(
      promiseFn,
      this.gettextCatalog.getString('Remove membership'),
      this.gettextCatalog.getString('Are you sure you want to remove the membership named <b>{{name}}</b>?',
                                    { name: this.membership.name }),
      this.gettextCatalog.getString('Remove failed'),
      this.gettextCatalog.getString('Membership with name {{name}} cannot be removed',
                                    { name: this.membership.name }));
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
      .catch(angular.noop);
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
      .catch(angular.noop);
  }

  getMatchingUserNames() {
    return (viewValue) =>
      this.UserName.list({ filter: 'name:like:' + viewValue}, this.membership.userData)
      .then((nameObjs) => nameObjs.map((nameObj) => ({ label: nameObj.name, obj: nameObj })));
  }

  addUser() {
    this.asyncInputModal.open(this.gettextCatalog.getString('Add user to membership'),
                              this.gettextCatalog.getString('User'),
                              this.gettextCatalog.getString('enter a user\'s name or partial name'),
                              this.gettextCatalog.getString('No matching users found'),
                              this.getMatchingUserNames()).result
      .then((modalValue) => {
        this.membership.addUser(modalValue.obj.id).then((membership) => {
          this.membership = membership;
          this.userNameLabels = this.userNamesToLabels();
        });
      })
      .catch(angular.noop);
  }

  // this method is invoked by a child component, so a callback function is returned
  // called to remove a user from the membership
  userLabelSelected() {
    return (userName) => {
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
          { name: userName.name }));
    }
  }

  getMatchingStudyNames() {
    return (viewValue) =>
      this.StudyName.list({ filter: 'name:like:' + viewValue}, this.membership.studyData.entityData)
      .then((nameObjs) =>
            nameObjs.map((nameObj) => ({ label: nameObj.name, obj: nameObj })));
  }

  addStudy() {
    this.asyncInputModal.open(this.gettextCatalog.getString('Add study to membership'),
                              this.gettextCatalog.getString('Study'),
                              this.gettextCatalog.getString('enter a study\'s name or partial name'),
                              this.gettextCatalog.getString('No matching studys found'),
                              this.getMatchingStudyNames()).result
      .then((modalValue) => {
        this.membership.addStudy(modalValue.obj.id).then((membership) => {
          this.membership = membership;
          this.studyNameLabels = this.studyNamesToLabels();
        });
      })
      .catch(angular.noop);
  }

  // this method is invoked by a child component, so a callback function is returned
  // called to remove a user from the membership
  studyLabelSelected() {
    return (studyName) => {
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
          { name: studyName.name }));
    }
  }

  getMatchingCentreNames() {
    return (viewValue) =>
      this.CentreName.list({ filter: 'name:like:' + viewValue}, this.membership.centreData.entityData)
      .then((nameObjs) => nameObjs.map((nameObj) => ({ label: nameObj.name, obj: nameObj })));

  }

  addCentre() {
    this.asyncInputModal.open(this.gettextCatalog.getString('Add centre to membership'),
                              this.gettextCatalog.getString('Centre'),
                              this.gettextCatalog.getString('enter a centre\'s name or partial name'),
                              this.gettextCatalog.getString('No matching centres found'),
                              this.getMatchingCentreNames()).result
      .then((modalValue) => {
        this.membership.addCentre(modalValue.obj.id).then((membership) => {
          this.membership = membership;
          this.centreNameLabels = this.centreNamesToLabels();
        });
      })
      .catch(angular.noop);
  }

  // this method is invoked by a child component, so a callback function is returned
  // called to remove a user from the membership
  centreLabelSelected() {
    return (centreName) => {
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
          { name: centreName.name }));
    }
  }

  back() {
    this.$state.go('home.admin.users.memberships');
  }

  updateMembership(membership) {
    this.membership       = membership;
    this.userNameLabels   = this.userNamesToLabels();
    this.studyNameLabels  = this.studyNamesToLabels();
    this.centreNameLabels = this.centreNamesToLabels();
  }

}

var component = {
  template: require('./membershipView.html'),
  controller: MembershipViewController,
  controllerAs: 'vm',
  bindings: {
    membership: '<'
  }
};

export default ngModule => ngModule.component('membershipView', component)
