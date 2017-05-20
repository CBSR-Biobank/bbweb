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
    'gettextCatalog',
    'notificationsService'
  ];

  function CollectionEventAnnotationTypeViewCtrl($q,
                                                 gettextCatalog,
                                                 notificationsService) {
    var vm = this;

    vm.onUpdate = onUpdate;

    function onUpdate(annotationType) {
      return vm.collectionEventType.addAnnotationType(annotationType)
        .then(postUpdate)
        .then(notifySuccess)
        .catch(notificationsService.updateError);
    }

    function postUpdate(collectionEventType) {
      vm.collectionEventType = collectionEventType;
      vm.annotationType = _.find(vm.collectionEventType.annotationTypes, { id: vm.annotationType.id });
      if (_.isUndefined(vm.annotationType)) {
        return $q.reject('could not update annotation type');
      }

      return $q.when(true);
    }

    function notifySuccess() {
      return notificationsService.success(
        gettextCatalog.getString('Annotation type changed successfully.'),
        gettextCatalog.getString('Change successful'),
        1500);
    }

  }

  return collectionEventAnnotationTypeViewDirective;

});
