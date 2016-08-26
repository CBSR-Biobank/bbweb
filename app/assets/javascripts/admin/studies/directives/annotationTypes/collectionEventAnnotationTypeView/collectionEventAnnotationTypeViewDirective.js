/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  /**
   *
   */
  function collectionEventAnnotationTypeViewDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study:               '=',
        collectionEventType: '=',
        annotationType:      '='
      },
      templateUrl: '/assets/javascripts/admin/studies/directives/annotationTypes/collectionEventAnnotationTypeView/collectionEventAnnotationTypeView.html',
      controller: CollectionEventAnnotationTypeViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CollectionEventAnnotationTypeViewCtrl.$inject = [
    '$q',
    'gettext',
    'notificationsService'
  ];

  function CollectionEventAnnotationTypeViewCtrl($q,
                                                 gettext,
                                                 notificationsService) {
    var vm = this;

    vm.onUpdate = onUpdate;

    function onUpdate(annotationType) {
      return vm.collectionEventType.updateAnnotationType(annotationType)
        .then(postUpdate)
        .then(notifySuccess)
        .catch(notificationsService.updateError);
    }

    function postUpdate(collectionEventType) {
      vm.collectionEventType = collectionEventType;
      vm.annotationType = _.find(vm.collectionEventType.annotationTypes,
                                 { uniqueId: vm.annotationType.uniqueId });
      if (_.isUndefined(vm.annotationType)) {
        return $q.reject('could not update annotation type');
      }

      return $q.when(true);
    }

    function notifySuccess() {
      return notificationsService.success(
        gettext('Annotation type changed successfully.'),
        gettext('Change successful'),
        1500);
    }

  }

  return collectionEventAnnotationTypeViewDirective;

});
