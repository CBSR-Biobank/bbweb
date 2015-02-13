/** Specimen Group helpers */
define(['./module'], function(module) {
  'use strict';

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  module.factory('UserViewer', UserViewerFactory);

  UserViewerFactory.$inject = ['EntityViewer'];

  function UserViewerFactory(EntityViewer) {

    function UserViewer(user) {
      this.ev = new EntityViewer(user, 'User');
      this.ev.addAttribute('Name', user.name);
      this.ev.addAttribute('Email',  user.email);
      this.ev.addAttribute('Status', user.status);

      this.ev.showModal();
    }

    return UserViewer;
  }

});
