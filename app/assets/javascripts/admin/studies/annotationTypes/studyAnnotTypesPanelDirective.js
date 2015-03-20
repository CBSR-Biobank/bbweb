define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  /**
   *
   */
  function studyAnnotTypesPanelDirective() {
    return {
      require: '^tab',
      restrict: 'E',
      scope: {
        study:           '=',
        annotTypes:      '=',
        annotTypesInUse: '=',
        annotTypeName:   '@',
        panelId:         '@',
        addStateName:    '@',
        updateStateName: '@'
      },
      templateUrl: '/assets/javascripts/admin/studies/annotationTypes/studyAnnotTypesPanel.html',
      controller: 'StudyAnnotTypesPanelCtrl as vm'
    };
  }

  StudyAnnotTypesPanelCtrl.$inject = [
    '$scope',
    'Panel',
    'StudyStatus'
  ];

  /**
   * A panel to display a study's participant annotation types.
   */
  function StudyAnnotTypesPanelCtrl($scope,
                                    Panel,
                                    StudyStatus) {
    var vm = this,
        panel = new Panel($scope.panelId, $scope.addStateName);

    vm.study                = $scope.study;
    vm.annotTypes           = $scope.annotTypes;
    vm.annotTypesInUse      = $scope.annotTypesInUse;
    vm.annotTypeName        = $scope.annotTypeName;
    vm.panelHeading         = panelHeading($scope.annotTypeName);
    vm.annotTypeDescription = annotTypeDescription($scope.annotTypeName);
    vm.updateStateName      = $scope.updateStateName;
    vm.add                  = add;
    vm.panelOpen            = panel.getPanelOpenState();
    vm.modificationsAllowed = (vm.study.status === StudyStatus.DISABLED());

    $scope.$watch(angular.bind(vm, function() { return vm.panelOpen; }),
                  angular.bind(panel, panel.watchPanelOpenChangeFunc));

    //--

    function add() {
      return panel.add();
    }

    function panelHeading(annotTypeName) {
      switch (annotTypeName) {
      case 'ParticipantAnnotationType':
        return 'Participant Annotation Types';

      case 'CollectionEventAnnotationType':
        return 'Collection Event Annotation Types';

      case 'SpecimenLinkAnnotationType':
        return 'Specimen Link Annotation Types';

      default:
        throw new Error('invalid annotation type name: ' + vm.annotTypeName);
      }
    }

    function annotTypeDescription(annotTypeName) {
      switch (annotTypeName) {
      case 'ParticipantAnnotationType':
        return 'Participant annotations allow a study to collect custom named and ' +
          'defined pieces of data for each participant. Annotations are optional and ' +
          'are not required to be defined.';

      case 'CollectionEventAnnotationType':
        return 'Collection event annotations allow a study to collect custom named and ' +
          'defined pieces of data for each collection event. Annotations are optional and ' +
          'are not required to be defined.';

      case 'SpecimenLinkAnnotationType':
        return 'Specimen link annotations allow a study to collect custom named and '+
          'defined pieces of data when processing specimens. Annotations are optional and ' +
          'are not required to be defined.';

      default:
        throw new Error('invalid annotation type name: ' + vm.annotTypeName);
      }

    }
  }

  return {
    directive: studyAnnotTypesPanelDirective,
    controller: StudyAnnotTypesPanelCtrl
  };
});
