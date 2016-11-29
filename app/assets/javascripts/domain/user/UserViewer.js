/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  UserViewerFactory.$inject = ['EntityViewer', 'gettextCatalog'];

  /**
   * Displays a study annotation type in a modal.
   *
   */
  function UserViewerFactory(EntityViewer, gettextCatalog) {

    function UserViewer(user) {
      this.ev = new EntityViewer(user, 'User');
      this.ev.addAttribute(gettextCatalog.getString('Name'),  user.name);
      this.ev.addAttribute(gettextCatalog.getString('Email'), user.email);
      this.ev.addAttribute(gettextCatalog.getString('State'), user.state.toUpperCase());

      this.ev.showModal();
    }

    return UserViewer;
  }

  return UserViewerFactory;
});
