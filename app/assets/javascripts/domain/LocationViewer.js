/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  LocationViewerFactory.$inject = ['EntityViewer'];

  /**
   *
   */
  function LocationViewerFactory(EntityViewer) {

    function LocationViewer(location) {
      var entityViewer = new EntityViewer(location, 'Location');

      entityViewer.addAttribute('Name',              location.name);
      entityViewer.addAttribute('Street',            location.street);
      entityViewer.addAttribute('City',              location.city);
      entityViewer.addAttribute('Province / State',  location.province);
      entityViewer.addAttribute('Postal / Zip code', location.postalCode);
      entityViewer.addAttribute('PO Box Number',     location.poBoxNumber);
      entityViewer.addAttribute('Country ISO Code',  location.countryIsoCode);

      entityViewer.showModal();
    }

    return LocationViewer;
  }

  return LocationViewerFactory;
});
