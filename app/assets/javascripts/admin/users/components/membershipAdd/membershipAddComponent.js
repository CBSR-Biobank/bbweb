/**
 * This component allows a user to add a new {@link domain.users.Membership|Membership} to the system.
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    template: require('./membershipAdd.html'),
    controller: MembershipAddController,
    controllerAs: 'vm',
    bindings: {
    }
  };

  MembershipAddController.$inject = [
    '$scope',
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
  function MembershipAddController($scope,
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

      // taken from here:
      // https://stackoverflow.com/questions/22436501/simple-angularjs-form-is-undefined-in-scope
      $scope.$watch('membershipForm', function () {
        $scope.membershipForm.$setValidity('studyOrCentreRequired', false);
      });
    }

    function getUserNames(viewValue) {
      var omitUserNames = vm.membership.userData.map(function (entityInfo) {
        return UserName.create(_.pick(entityInfo, ['id', 'name']));
      });
      return UserName.list({ filter: 'name:like:' + viewValue}, omitUserNames)
        .then(function (nameObjs) {
          var labelData = nameObjs.map(function (nameObj) {
            return { label: nameObj.name, obj: nameObj};
          });
          return labelData;
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
      var omitStudyNames = vm.membership.studyData.entityData.map(function (entityInfo) {
        return StudyName.create(_.pick(entityInfo, ['id', 'name']));
      });
      return StudyName.list({ filter: 'name:like:' + viewValue}, omitStudyNames)
        .then(function (names) {
          return names.map(function (name) {
            return { label: name.name, obj: new EntityInfo({ id: name.id, name: name.name }) };
          });
        });
    }

    function studySelected(selection) {
      vm.membership.studyData.entityData.push(selection);
      $scope.membershipForm.$setValidity('studyOrCentreRequired', true);
    }

    function removeStudy(studyTag) {
      _.remove(vm.membership.studyData.entityData, function (studyData) {
        return studyData.name === studyTag.name;
      });
      setValidity();
    }

    function getCentreNames(viewValue) {
      var omitCentreNames = vm.membership.centreData.entityData.map(function (entityInfo) {
        return StudyName.create(_.pick(entityInfo, ['id', 'name']));
      });
      return CentreName.list({ filter: 'name:like:' + viewValue}, omitCentreNames)
        .then(function (names) {
          return names.map(function (name) {
            return { label: name.name, obj: new EntityInfo({ id: name.id, name: name.name }) };
          });
        });
    }

    function centreSelected(selection) {
      vm.membership.centreData.entityData.push(selection);
      $scope.membershipForm.$setValidity('studyOrCentreRequired', true);
    }

    function removeCentre(centreTag) {
      _.remove(vm.membership.centreData.entityData, function (centreData) {
        return centreData.name === centreTag.name;
      });
      setValidity();
    }

    function setValidity() {
      if (vm.membership.studyData.allEntities || vm.membership.centreData.allEntities) { return; }

      if ((vm.membership.studyData.entityData.length <= 0) &&
          (vm.membership.centreData.entityData.length <= 0)) {
        $scope.membershipForm.$setValidity('studyOrCentreRequired', false);
      }
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
