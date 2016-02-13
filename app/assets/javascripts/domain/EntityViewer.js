/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  EntityViewerFactory.$inject = ['$uibModal'];

  /**
   * Factory for EntityViewer.
   */
  function EntityViewerFactory($uibModal) {

    /**
     * Used to display a domain entity in a modal.
     *
     * The modal will display all the attributes added with method 'addAttribute' and also the
     * timeAdded and timeModified values from the entity.
     *
     * The modal shows the items in the order they are added.
     */
    function EntityViewer(entity, title) {
      var self = this;
      if (arguments.length === 0) { return; }
      self.entity = entity;
      self.title = title;
      self.items = [];
      self.timeAdded = entity.timeAdded;
      self.timeModified = entity.timeModified;
    }

    EntityViewer.prototype.addAttribute = function (label, value) {
      this.items.push({ label: label, value: value});
    };

    /**
     * @param data an array of objects with 2 attributes: name and value.
     */
    EntityViewer.prototype.showModal = function () {
      var self = this;

      var modalOptions = {
        backdrop: true,
        keyboard: true,
        modalFade: true,
        templateUrl: '/assets/javascripts/domain/entityViewer.html',
        controller: controller
      };

      controller.$inject = ['$scope', '$uibModalInstance'];

      function controller($scope, $uibModalInstance) {

        $scope.modalOptions = {
          entityViewer: self,
          ok: ok
        };

        //--

        function ok() {
          $uibModalInstance.close();
        }
      }

      return $uibModal.open(modalOptions).result;
    };


    /** return constructor function */
    return EntityViewer;
  }

  return EntityViewerFactory;
});
