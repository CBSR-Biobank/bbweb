/* global define */
define(['./module'], function(module) {
  'use strict';

  module.factory('EntityViewer', EntityViewerFactory);

  EntityViewerFactory.$inject = ['$modal'];

  /**
   * Factory for EntityViewer.
   */
  function EntityViewerFactory($modal) {

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
        templateUrl: '/assets/javascripts/common/services/domainEntityModal.html',
        controller: controller
      };

      controller.$inject = ['$scope', '$modalInstance'];

      function controller($scope, $modalInstance) {

        $scope.modalOptions = {
          entityViewer: self,
          ok: ok
        };

        //--

        function ok() {
          $modalInstance.close();
        }
      }

      return $modal.open(modalOptions).result;
    };


    /** return constructor function */
    return EntityViewer;
  }

});
