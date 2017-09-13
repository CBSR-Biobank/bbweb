/**
 * This component allows a user to add a new {@link domain.users.Membership|Membership} to the system.
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl: '/assets/javascripts/admin/users/components/membershipAdd/membershipAdd.html',
    controller: MembershipAddController,
    controllerAs: 'vm',
    bindings: {
    }
  };

  MembershipAddController.$inject = [
    '$state',
    'notificationsService',
    'domainNotificationService',
    'Membership',
    'breadcrumbService',
    'gettextCatalog',
    'EntityInfo',
    'UserName',
    'StudyName',
    'CentreName'
  ];

  /*
   *
   */
  function MembershipAddController($state,
                                   notificationsService,
                                   domainNotificationService,
                                   Membership,
                                   breadcrumbService,
                                   gettextCatalog,
                                   EntityInfo,
                                   UserName,
                                   StudyName,
                                   CentreName) {
    var vm = this,
        returnState = 'home.admin.users.memberships';

    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.breadcrumbs = [
        breadcrumbService.forState('home'),
        breadcrumbService.forState('home.admin'),
        breadcrumbService.forState('home.admin.users'),
        breadcrumbService.forState('home.admin.users.memberships'),
        breadcrumbService.forState('home.admin.users.memberships.add')
      ];

      vm.membership = new Membership();
      vm.submit = submit;
      vm.cancel = cancel;

      vm.getUserNames = getUserNames;
      vm.userSelected = userSelected;
      vm.removeUser   = removeUser;

      vm.getStudyNames = getStudyNames;
      vm.studySelected = studySelected;
      vm.removeStudy   = removeStudy;

      vm.getCentreNames = getCentreNames;
      vm.centreSelected = centreSelected;
      vm.removeCentre   = removeCentre;
    }

    function getUserNames(viewValue) {
      return UserName.list({ filter: 'name:like:' + viewValue}).then(function (names) {
        // remove names already added from the reply
        return _.differenceWith(names, vm.membership.userData,function (userName, entityInfo) {
          return userName.name === entityInfo.name;
        }).map(function (name) {
          return { label: name.name, obj: new EntityInfo({ id: name.id, name: name.name }) };
        });
      });
    }

    function userSelected(selection) {
      vm.membership.userData.push(selection);
    }

    function removeUser(userTag) {
      _.remove(vm.membership.userData, function (userData) {
        return userData.name === userTag.name;
      });
    }

    function getStudyNames(viewValue) {
      return StudyName.list({ filter: 'name:like:' + viewValue}).then(function (names) {
        // remove names already added from the reply
        return _.differenceWith(names, vm.membership.studyData.entityData,function (studyName, entityInfo) {
          return studyName.name === entityInfo.name;
        }).map(function (name) {
          return { label: name.name, obj: new EntityInfo({ id: name.id, name: name.name }) };
        });
      });
    }

    function studySelected(selection) {
      vm.membership.studyData.entityData.push(selection);
    }

    function removeStudy(studyTag) {
      _.remove(vm.membership.studyData.entityData, function (studyData) {
        return studyData.name === studyTag.name;
      });
    }

    function getCentreNames(viewValue) {
      return CentreName.list({ filter: 'name:like:' + viewValue}).then(function (names) {
        // remove names already added from the reply
        return _.differenceWith(names, vm.membership.centreData.entityData,function (centreName, entityInfo) {
          return centreName.name === entityInfo.name;
        }).map(function (name) {
          return { label: name.name, obj: new EntityInfo({ id: name.id, name: name.name }) };
        });
      });
    }

    function centreSelected(selection) {
      vm.membership.centreData.entityData.push(selection);
    }

    function removeCentre(centreTag) {
      _.remove(vm.membership.centreData.entityData, function (centreData) {
        return centreData.name === centreTag.name;
      });
    }

    function submit() {
      vm.membership.add().then(onSubmitSuccess).catch(onSubmitError);
    }

    function onSubmitSuccess() {
      notificationsService.submitSuccess();
      $state.go(returnState, {}, { reload: true });
    }

    function onSubmitError(error) {
      domainNotificationService.updateErrorModal(error, gettextCatalog.getString('centre'));
    }

    function cancel() {
      $state.go(returnState);
    }

  }

  return component;
});
