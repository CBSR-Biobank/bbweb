/**
 *
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      _       = require('lodash');

  var component = {
    template: require('./membershipView.html'),
    controller: MembershipViewController,
    controllerAs: 'vm',
    bindings: {
      membership: '<'
    }
  };

  MembershipViewController.$inject = [
    '$state',
    'notificationsService',
    'domainNotificationService',
    'gettextCatalog',
     'breadcrumbService',
    'usersService',
    'modalInput',
    'asyncInputModal',
    'EntityInfo',
    'UserName'
  ];

  /*
   *
   */
  function MembershipViewController($state,
                                    notificationsService,
                                    domainNotificationService,
                                    gettextCatalog,
                                    breadcrumbService,
                                    usersService,
                                    modalInput,
                                    asyncInputModal,
                                    EntityInfo,
                                    UserName) {
    var vm = this;
    vm.$onInit = onInit;

    vm.remove          = remove;
    vm.editName        = editName;
    vm.editDescription = editDescription;
    vm.addUser         = addUser;
    vm.addStudy        = addStudy;
    vm.addCentre       = addCentre;
    vm.back            = back;

    //--

    function onInit() {
      vm.userCanUpdate = usersService.getCurrentUser().hasRole('UserAdministrator');

      vm.breadcrumbs = [
        breadcrumbService.forState('home'),
        breadcrumbService.forState('home.admin'),
        breadcrumbService.forState('home.admin.users'),
        breadcrumbService.forState('home.admin.users.memberships'),
        breadcrumbService.forStateWithFunc('home.admin.users.memberships.membership', membershipName)
      ];

      vm.noStudiesMembership = (!vm.membership.studyData.allEntities &&
                                (vm.membership.studyData.entityData.length <= 0));

      vm.noCentresMembership = (!vm.membership.centreData.allEntities &&
                                (vm.membership.centreData.entityData.length <= 0));

      vm.userNameLabels = userNameLabels();
      vm.studyNameLabels = studyNameLabels();
      vm.centreNameLabels = centreNameLabels();

      vm.userNameLabelSelected = userNameLabelSelected;
    }

    function membershipName() {
      return vm.membership.name;
    }

    function entityNameToLabels(entityData) {
      return _.sortBy(entityData.map(function (userInfo) {
        return {
          label:   userInfo.name,
          tooltip: gettextCatalog.getString('Remove ' + userInfo.name),
          obj:     userInfo
        };
      }), [ 'label' ]);
    }

    function userNameLabels() {
      return entityNameToLabels(vm.membership.userData);
    }

    function studyNameLabels() {
      return entityNameToLabels(vm.membership.studyData.entityData);
    }

    function centreNameLabels() {
      return entityNameToLabels(vm.membership.centreData.entityData);
    }

    function remove() {
      domainNotificationService.removeEntity(
        promiseFn,
        gettextCatalog.getString('Remove membership'),
        gettextCatalog.getString('Are you sure you want to remove the membership named <b>{{name}}</b>?',
                                 { name: vm.membership.name }),
        gettextCatalog.getString('Remove failed'),
        gettextCatalog.getString('Membership with name {{name}} cannot be removed',
                                 { name: vm.membership.name }));

      function promiseFn() {
        return vm.membership.remove().then(function () {
          notificationsService.success(gettextCatalog.getString('Membership removed'));
          $state.go('home.admin.users.memberships', {}, { reload: true });
        });
      }
    }

    function postUpdate(message, title, timeout) {
      timeout = timeout || 1500;
      return function (membership) {
        vm.membership = membership;
        notificationsService.success(message, title, timeout);
      };
    }

    function editName() {
      modalInput.text(gettextCatalog.getString('Membership name'),
                      gettextCatalog.getString('Name'),
                      vm.membership.name,
                      { required: true, minLength: 2 }).result
        .then(
          function (name) {
            vm.membership.updateName(name)
              .then(postUpdate(gettextCatalog.getString('Name changed successfully.'),
                               gettextCatalog.getString('Change successful')))
              .catch(notificationsService.updateError);
          },
          angular.noop);
    }

    function editDescription() {
      modalInput.textArea(gettextCatalog.getString('Membership description'),
                      gettextCatalog.getString('Description'),
                      vm.membership.description).result
        .then(
          function (description) {
            vm.membership.updateDescription(description)
              .then(postUpdate(gettextCatalog.getString('Description changed successfully.'),
                               gettextCatalog.getString('Change successful')))
              .catch(notificationsService.updateError);
          },
          angular.noop);
    }

    function addUser() {
      asyncInputModal.open(gettextCatalog.getString('Add user to membership'),
                           gettextCatalog.getString('User'),
                           gettextCatalog.getString('enter a user\'s name or partial name'),
                           gettextCatalog.getString('No matching users found'),
                           getResults).result
        .then(addUserToMembership, angular.noop);

      function getResults(viewValue) {
        return UserName.list({ filter: 'name:like:' + viewValue}, vm.membership.userData)
          .then(function (nameObjs) {
            return nameObjs.map(function (nameObj) {
              return { label: nameObj.name, obj: nameObj };
            });
          });
      }

      function addUserToMembership(modalValue) {
        vm.membership.addUser(modalValue.obj.id).then(function (membership) {
          vm.membership = membership;
          vm.userNameLabels = userNameLabels();
        });
      }
    }

    function userNameLabelSelected(userName) {
      domainNotificationService.removeEntity(
        promiseFn,
        gettextCatalog.getString('Remove user from membership'),
        gettextCatalog.getString(
          'Are you sure you want to remove the user named <strong>{{name}}</strong> from this membership?',
          { name: userName.name }),
        gettextCatalog.getString('Remove failed'),
        gettextCatalog.getString(
          'User named {{name}} cannot be removed',
          { name: userName.name }));

      function promiseFn() {
        return vm.membership.removeUser(userName.id).then(function (membership) {
          notificationsService.success(gettextCatalog.getString(
            'User {{name}} removed',
            { name: userName.name }));
          updateMembership(membership);
        });
      }
    }


    function addStudy() {
    }

    function addCentre() {
    }

    function back() {
      $state.go('home.admin.users.memberships');
    }

    function updateMembership(membership) {
      vm.membership = membership;
      vm.userNameLabels = userNameLabels();
      vm.studyNameLabels = studyNameLabels();
      vm.centreNameLabels = centreNameLabels();
    }

  }

  return component;
});
