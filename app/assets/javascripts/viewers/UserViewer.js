/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * Displays a study annotation type in a modal.
 *
 */
/* @ngInject */
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

export default ngModule => ngModule.factory('UserViewer', UserViewerFactory)
