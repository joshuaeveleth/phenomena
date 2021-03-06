package com.axiomalaska.phenomena;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import ucar.units.BaseUnit;
import ucar.units.SI;
import ucar.units.Unit;

import com.axiomalaska.cf4j.CFStandardName;
import com.axiomalaska.cf4j.CFStandardNames;
import com.axiomalaska.ioos.parameter.IoosParameter;
import com.axiomalaska.ioos.sos.IoosSosUtil;
import com.hp.hpl.jena.ontology.Individual;

public class Phenomena {
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CFParameter {}

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IOOSParameter {}
    
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface HomelessParameter {
        public String description();
        public String source();
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NonStandardUnits {}

    private static Phenomena instance;
    private final UnitResolver unitResolver = UnitResolver.instance();
    private final List<Phenomenon> allPhenomena = new ArrayList<Phenomenon>();

    public Phenomena() throws UnitCreationException {}

    public static Phenomena instance() throws UnitCreationException{
        if( instance == null ){
            instance = new Phenomena();
        }
        return instance;
    }

    public static final String IOOS_MMI_URL_PREFIX = IoosParameter.NS;
    public static final String CF_MMI_URL_PREFIX = "http://mmisw.org/ont/cf/parameter/";
    public static final String GENERIC_FAKE_MMI_URL_PREFIX = "http://mmisw.org/ont/fake/parameter/";

    public List<Phenomenon> getAllPhenomena(){
        return allPhenomena;
    }

    /**
     * All other phenomenon creation methods should use this method so that the phenomenon
     * gets added to the allPhenomena list. Note: this should be the only method in Phenomena
     * to call PhenomenonImp's constructor
     * 
     * @param name
     * @param id
     * @param unit
     * @return the created phenomenon
     */

    private final Phenomenon createPhenomenon( String name, String id, Unit unit ){
        String tag = name;
        String parsedTag = IoosSosUtil.getNameFromUri(id);
        if (!parsedTag.equals(id)) {
            tag = parsedTag;
        }
        
        Phenomenon phenomenon = new PhenomenonImp( name, id, tag, unit );
        allPhenomena.add( phenomenon );
        return phenomenon;
    }

    private final Phenomenon createPhenomenon( String name, String id ){
        Unit unit = null;
        return createPhenomenon(name, id, unit);
    }
    
    private final Phenomenon createPhenomenon( String name, String id, String unit )
            throws UnitCreationException{
        return createPhenomenon(
             name
            ,id
            ,unitResolver.resolveUnit( unit )
        );
    }

    private final Phenomenon createCfParameterWithAlternateName(
            CFStandardName cfStandardName, String name ) throws UnitCreationException{
        PhenomenonImp sosPhen = (PhenomenonImp) createStandardCfParameter( cfStandardName );
        sosPhen.setName( name );
        return sosPhen;
    }

    @SuppressWarnings("unused")
    private final Phenomenon createCfParameterWithAlternateUnits(
            CFStandardName cfStandardName, String unitString ) throws UnitCreationException{
        return createPhenomenon(
             IoosSosUtil.convertUnderscoredNameToTitleCase(cfStandardName.getName())
            ,CF_MMI_URL_PREFIX + cfStandardName.getName()
            ,unitResolver.resolveUnit( unitString )
        );
    }
    
    private final Phenomenon createCfParameterWithAlternateUnits(
            CFStandardName cfStandardName, Unit unit ){
        return createPhenomenon(
             IoosSosUtil.convertUnderscoredNameToTitleCase(cfStandardName.getName())
            ,CF_MMI_URL_PREFIX + cfStandardName.getName()
            ,unit
        );
    }

    @SuppressWarnings("unused")
    private final Phenomenon createIoosParameterWithAlternateUnits(
            Individual ioosParameter, String unitString ) throws UnitCreationException{
        return createPhenomenon(
             IoosSosUtil.convertUnderscoredNameToTitleCase(ioosParameter.getLocalName())
            ,ioosParameter.getURI()
            ,unitResolver.resolveUnit( unitString )
        );
    }
    
    private final Phenomenon createIoosParameterWithAlternateUnits(
            Individual ioosParameter, Unit unit ){
        return createPhenomenon(
             IoosSosUtil.convertUnderscoredNameToTitleCase(ioosParameter.getLocalName())
            ,ioosParameter.getURI()
            ,unit
        );
    }

    private final Phenomenon createStandardCfParameter( CFStandardName cfStandardName )
            throws UnitCreationException{
        return createPhenomenon(
             IoosSosUtil.convertUnderscoredNameToTitleCase(cfStandardName.getName())
            ,CF_MMI_URL_PREFIX + cfStandardName.getName()
            ,unitResolver.resolveUnit( cfStandardName.getCanonicalUnits() )
        );
    }
    
    private final Phenomenon createStandardIoosParameter( Individual ioosParameter )
            throws UnitCreationException{
        String unitString = IoosParameterUtil.getInstance().getPropertyValue(
                ioosParameter, IoosParameter.Units );
        Unit unit = unitString != null ? unitResolver.resolveUnit( unitString ) : BaseUnit.DIMENSIONLESS;
        return createPhenomenon(
             IoosSosUtil.convertUnderscoredNameToTitleCase(ioosParameter.getLocalName())
            ,ioosParameter.getURI()
            ,unit
        );
    }    
    
    @CFParameter
    public final Phenomenon AIR_PRESSURE = createStandardCfParameter( CFStandardNames.AIR_PRESSURE );
    
    @CFParameter
    public final Phenomenon AIR_PRESSURE_AT_SEA_LEVEL = createStandardCfParameter( CFStandardNames.AIR_PRESSURE_AT_SEA_LEVEL );
    
    @CFParameter
    public final Phenomenon DOWNWELLING_SHORTWAVE_FLUX_IN_AIR = createStandardCfParameter(CFStandardNames.DOWNWELLING_SHORTWAVE_FLUX_IN_AIR);
    
    @CFParameter
    @NonStandardUnits
    public final Phenomenon AIR_TEMPERATURE = createCfParameterWithAlternateUnits(
            CFStandardNames.AIR_TEMPERATURE
            , SI.DEGREE_CELSIUS
    );
    

    @HomelessParameter(description="",source="NERRS, RAWS, SnoTel")
    public final Phenomenon AIR_TEMPERATURE_AVERAGE = createPhenomenon(
            "Air Temperature Average"
            , GENERIC_FAKE_MMI_URL_PREFIX + "air_temperature_average"
            , SI.DEGREE_CELSIUS
    );

    @HomelessParameter(description="",source="NDBC")
    public final Phenomenon DEPTH = createPhenomenon(
            "Depth"
            , GENERIC_FAKE_MMI_URL_PREFIX + "depth"
            , SI.METER
    );

    @HomelessParameter(description="",source="Cal Poly Met")
    public final Phenomenon ATMOSPHERE_NET_RATE_OF_ABSORPTION_OF_ENERGY = createPhenomenon(
            "Atmosphere Net Rate Of Absorption of Energy"
            , GENERIC_FAKE_MMI_URL_PREFIX + "atmosphere_net_rate_of_absorption_of_energy"
            , CustomUnits.instance().WATTS_PER_SQUARE_METER
    );

    @HomelessParameter(description="",source="Cal Poly Shore")
    public final Phenomenon TRANSMISSOMETER = createPhenomenon(
            "transmissometer"
            , GENERIC_FAKE_MMI_URL_PREFIX + "transmissometer"
            , SI.VOLT
    );

    @HomelessParameter(description="",source="HADS, RAWS, SnoTel")
    public final Phenomenon AIR_TEMPERATURE_MAXIMUM = createPhenomenon(
        "Air Temperature Maximum"
       ,GENERIC_FAKE_MMI_URL_PREFIX + "air_temperature_maximum"
       ,SI.DEGREE_CELSIUS
    );

    @HomelessParameter(description="",source="HADS, RAWS, SnoTel")
    public final Phenomenon AIR_TEMPERATURE_MINIMUM = createPhenomenon(
        "Air Temperature Minimum"
       ,GENERIC_FAKE_MMI_URL_PREFIX + "air_temperature_minimum"
       ,SI.DEGREE_CELSIUS
    );
    
    @HomelessParameter(description="",source="NERRS, RAWS, SnoTel")
    public final Phenomenon FLUORESCENCE = createPhenomenon(
            "Fluorescence"
            , GENERIC_FAKE_MMI_URL_PREFIX + "fluorescence"
            , "RFU"
    );

    @CFParameter
    public final Phenomenon ALTITUDE = createStandardCfParameter(CFStandardNames.ALTITUDE);

    @CFParameter
    public final Phenomenon SEA_WATER_DENSITY = createStandardCfParameter( CFStandardNames.SEA_WATER_DENSITY);

    @CFParameter
    public final Phenomenon SURFACE_TEMPERATURE = createStandardCfParameter( CFStandardNames.SURFACE_TEMPERATURE);
    
    @IOOSParameter
    public final Phenomenon AMMONIUM =
        createStandardIoosParameter(IoosParameter.ammonium);
    
    @IOOSParameter
    public final Phenomenon BATTERY_VOLTAGE =
        createStandardIoosParameter( IoosParameter.battery_voltage );

    @IOOSParameter
    public final Phenomenon PCO2 =
        createStandardIoosParameter( IoosParameter.pCO2 );
    
    @IOOSParameter
    public final Phenomenon OMEGA_ARAGONITE =
    		createPhenomenon(
                    "Omega Aragonite"
                    , IOOS_MMI_URL_PREFIX + "omega_aragonite"
                    , "uatm");
    
    @IOOSParameter
    public final Phenomenon DISSOLVED_CO2 =
    		createPhenomenon(
    		        "Dissolved CO2"
    		       ,IOOS_MMI_URL_PREFIX + "dissolved_carbon_dioxide_co2"
    		       ,CustomUnits.instance().MILLIGRAMS_PER_LITER);

    @HomelessParameter(description="",source="SnoTel")
    public final Phenomenon BATTERY_VOLTAGE_MAXIMUM = createPhenomenon(
        "Battery Voltage Maximum"
       ,GENERIC_FAKE_MMI_URL_PREFIX + "battery_voltage_maximum"
       ,SI.VOLT       
    );

    @HomelessParameter(description="",source="Marine Calpoly Shore")
    public final Phenomenon BIOLUMINESCENCE = createPhenomenon(
            "Bioluminescence"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "bioluminescence"
            ,"photon.s-1"
    );
    
    @HomelessParameter(description="",source="SnoTel")
    public final Phenomenon BATTERY_VOLTAGE_MINIMUM = createPhenomenon(
        "Battery Voltage Maximum"
       ,GENERIC_FAKE_MMI_URL_PREFIX + "battery_voltage_minimum"
       ,SI.VOLT
    );
    
    @IOOSParameter
    public final Phenomenon CHLOROPHYLL_FLOURESCENCE =
        createStandardIoosParameter( IoosParameter.chlorophyll_flourescence );

    @HomelessParameter(description="",source="USGS")
    public final Phenomenon DEPTH_TO_WATER_LEVEL = createPhenomenon(
         "Depth to Water Level"
        ,GENERIC_FAKE_MMI_URL_PREFIX + "depth_to_water_level"
        ,SI.METER
    );

    //http://mmisw.org/ont/cf/parameter/downwelling_photosynthetic_photon_flux_in_air micromol.m-2.s-1

    @CFParameter
    public final Phenomenon SURFACE_DOWNWELLING_PHOTOSYNTHETIC_PHOTON_FLUX_IN_AIR = createCfParameterWithAlternateUnits(
            CFStandardNames.SURFACE_DOWNWELLING_PHOTOSYNTHETIC_PHOTON_FLUX_IN_AIR, "mol.m-2.s-1"
    );

    @CFParameter
    public final Phenomenon SURFACE_DOWNWELLING_SHORTWAVE_FLUX_IN_AIR = createStandardCfParameter(
            CFStandardNames.SURFACE_DOWNWELLING_SHORTWAVE_FLUX_IN_AIR
    );

    @CFParameter
    @NonStandardUnits
    public final Phenomenon DEW_POINT_TEMPERATURE = createCfParameterWithAlternateUnits(
         CFStandardNames.DEW_POINT_TEMPERATURE
        ,SI.DEGREE_CELSIUS
    );

    @CFParameter
    public final Phenomenon DIRECTION_OF_SEA_WATER_VELOCITY
        = createStandardCfParameter( CFStandardNames.DIRECTION_OF_SEA_WATER_VELOCITY );
    
    @HomelessParameter(description="",source="NOAA NOS CO-OPS, NDBC")
    public final Phenomenon DOMINANT_WAVE_PERIOD = createPhenomenon(
         "Sea Surface Dominant Wave Period"
        ,GENERIC_FAKE_MMI_URL_PREFIX + "sea_surface_dominant_wave_period"
        ,SI.SECOND
    );
    
    @CFParameter
    public final Phenomenon DOWNWELLING_PHOTOSYNTHETIC_RADIATIVE_FLUX_IN_SEA_WATER =
        createStandardCfParameter( CFStandardNames.DOWNWELLING_PHOTOSYNTHETIC_RADIATIVE_FLUX_IN_SEA_WATER );
    
    @CFParameter
    public final Phenomenon DOWNWELLING_LONGWAVE_FLUX_IN_AIR =
        createStandardCfParameter( CFStandardNames.DOWNWELLING_LONGWAVE_FLUX_IN_AIR );

    @CFParameter
    @NonStandardUnits
    public final Phenomenon FRACTIONAL_SATURATION_OF_OXYGEN_IN_SEA_WATER = createCfParameterWithAlternateUnits(
         CFStandardNames.FRACTIONAL_SATURATION_OF_OXYGEN_IN_SEA_WATER
        ,CustomUnits.instance().PERCENTAGE
    );

    @HomelessParameter(description="",source="HADS, RAWS")
    public final Phenomenon FUEL_MOISTURE = createPhenomenon(
         "Fuel Moisture"
        ,GENERIC_FAKE_MMI_URL_PREFIX + "fuel_moisture"
        ,CustomUnits.instance().PERCENTAGE
    );

    @HomelessParameter(description="",source="HADS, RAWS")
    public final Phenomenon FUEL_TEMPERATURE = createPhenomenon(
         "Fuel Temperature"
        ,GENERIC_FAKE_MMI_URL_PREFIX + "fuel_temperature"
        ,SI.DEGREE_CELSIUS
    );

    @CFParameter
    public final Phenomenon GRID_LATITUDE = createStandardCfParameter( CFStandardNames.GRID_LATITUDE );

    @CFParameter
    public final Phenomenon GRID_LONGITUDE = createStandardCfParameter( CFStandardNames.GRID_LONGITUDE );
    
    @CFParameter
    public final Phenomenon HEIGHT = createStandardCfParameter( CFStandardNames.HEIGHT );        
    
    @CFParameter
    public final Phenomenon LWE_THICKNESS_OF_PRECIPITATION_AMOUNT = createCfParameterWithAlternateName(
         CFStandardNames.LWE_THICKNESS_OF_PRECIPITATION_AMOUNT
        ,"Precipitation"
    );    

    @CFParameter
    public final Phenomenon MASS_CONCENTRATION_OF_CARBON_DIOXIDE_IN_AIR =
        createStandardCfParameter( CFStandardNames.MASS_CONCENTRATION_OF_CARBON_DIOXIDE_IN_AIR );
    
    @HomelessParameter(description="",source="NOAA PMEL")
    public final Phenomenon PPM_OF_CARBON_DIOXIDE_IN_AIR= createPhenomenon(
         "Carbon Dioxide in air PPM"
        ,GENERIC_FAKE_MMI_URL_PREFIX + "ppm_of_carbon_dioxide_in_air"
        ,"ppm"
    );
    
    @HomelessParameter(description="",source="")
    public final Phenomenon MASS_CONCENTRATION_OF_CARBON_DIOXIDE_IN_SEA_WATER = createPhenomenon(
        "Mass Concentration of Carbon Dioxide in Sea Water"
       ,GENERIC_FAKE_MMI_URL_PREFIX + "mass_concentration_of_carbon_dioxide_in_sea_water"
       ,CustomUnits.instance().KILOGRAMS_PER_CUBIC_METER
    );

    @HomelessParameter(description="",source="")
    public final Phenomenon PPM_OF_CARBON_DIOXIDE_IN_SEA_WATER_IN_WET_GAS = createPhenomenon(
            "PPM of Carbon Dioxide in Sea Water in wet gas"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "ppm_of_carbon_dioxide_in_sea_water_in_wet_gas"
            ,"mg.L-1"
    );

    @HomelessParameter(description="",source="")
    public final Phenomenon PPM_OF_CARBON_DIOXIDE_IN_AIR_IN_DRY_GAS = createPhenomenon(
            "PPM of Carbon Dioxide in air in dry gas"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "ppm_of_carbon_dioxide_in_air_in_dry_gas"
            ,"1e-6"
    );

    @HomelessParameter(description="",source="")
    public final Phenomenon PPT_OF_WATER_VAPOR_IN_SEA_WATER = createPhenomenon(
            "PPT of water vapor in sea water"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "ppt_of_water_vapor_in_sea_water"
            ,"1e-3"
    );

    @HomelessParameter(description="",source="")
    public final Phenomenon PPT_OF_WATER_VAPOR_IN_AIR= createPhenomenon(
            "PPT of water vapor in air"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "ppt_of_water_vapor_in_air"
            ,"1e-3"
    );

    @HomelessParameter(description="",source="")
    public final Phenomenon FUGACITY_OF_CARBON_DIOXIDE_IN_SEA_WATER = createPhenomenon(
            "Fugacity of Carbon Dioxide in Sea Water"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "fugacity_of_carbon_dioxide_in_sea_water"
            ,"microatm"
    );

    @HomelessParameter(description="",source="")
    public final Phenomenon CARBON_DIOXIDE_FUGACITY_DIFFERENCE_BETWEEN_AIR_AND_SEA_WATER = createPhenomenon(
            "Carbon Dioxide Fugacity difference between Air and sea water"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "carbon_dioxide_fugacity_difference_between_air_and_sea_water"
            ,"microatm"
    );

    @HomelessParameter(description="",source="")
    public final Phenomenon FUGACITY_OF_CARBON_DIOXIDE_IN_AIR = createPhenomenon(
            "Fugacity of Carbon Dioxide in Air"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "fugacity_of_carbon_dioxide_in_air"
            ,"microatm"
    );

    @HomelessParameter(description="",source="")
    public final Phenomenon PCO2_IN_SEA_WATER = createPhenomenon(
            "PCO2 in sea water"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "pCO2_in_sea_water"
            ,"microatm"
    );

    @HomelessParameter(description="",source="")
    public final Phenomenon PCO2_IN_AIR = createPhenomenon(
            "PCO2 in air"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "pCO2_in_air"
            ,"microatm"
    );

    @HomelessParameter(description="",source="")
    public final Phenomenon PCO2_DIFFERENCE_BETWEEN_AIR_AND_SEA_WATER = createPhenomenon(
            "pCO2 difference between air and sea water"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "pCO2_difference_between_air_and_sea_water"
            ,"microatm"
    );

    @HomelessParameter(description="",source="")
    public final Phenomenon VOLUME_FRACTION_OF_OXYGEN_IN_SEA_WATER = createPhenomenon(
            "volume fraction of oxygen in sea water"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "volume_fraction_of_oxygen_in_sea_water"
            ,"%"
    );

    @HomelessParameter(description="",source="")
    public final Phenomenon TOTAL_DISSOLVED_SOLIDS = createPhenomenon(
            "total dissolved solids"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "total_dissolved_solids"
            ,"g.L-1"
    );
    
    @CFParameter
    public final Phenomenon MASS_CONCENTRATION_OF_CHLOROPHYLL_IN_SEA_WATER =
        createStandardCfParameter( CFStandardNames.MASS_CONCENTRATION_OF_CHLOROPHYLL_IN_SEA_WATER );

    @CFParameter
    public final Phenomenon MASS_CONCENTRATION_OF_OXYGEN_IN_SEA_WATER =
        createStandardCfParameter( CFStandardNames.MASS_CONCENTRATION_OF_OXYGEN_IN_SEA_WATER );

    @CFParameter
    public final Phenomenon MOLE_CONCENTRATION_OF_NITRATE_IN_SEA_WATER = createStandardCfParameter( CFStandardNames.MOLE_CONCENTRATION_OF_NITRATE_IN_SEA_WATER);

    @IOOSParameter
    public final Phenomenon NITRATE =
        createStandardIoosParameter( IoosParameter.nitrate );
    
    @IOOSParameter
    public final Phenomenon NITRITE =
        createStandardIoosParameter( IoosParameter.nitrite );
    
    @IOOSParameter
    public final Phenomenon NITRITE_PLUS_NITRATE =
        createStandardIoosParameter( IoosParameter.nitrite_plus_nitrate );

    @CFParameter
    public final Phenomenon NORTHWARD_SEA_WATER_VELOCITY =
            createStandardCfParameter( CFStandardNames.NORTHWARD_SEA_WATER_VELOCITY );

    @HomelessParameter(description="",source="")
    public final Phenomenon PANEL_TEMPERATURE = createPhenomenon(
         "Panel Temperature"
        ,GENERIC_FAKE_MMI_URL_PREFIX + "panel_temperature"
        ,SI.DEGREE_CELSIUS
    );
    
    @IOOSParameter
    public final Phenomenon PEAK_WAVE_DIRECTION =
        createStandardIoosParameter( IoosParameter.peak_wave_direction );

    @IOOSParameter
    public final Phenomenon PEAK_WAVE_PERIOD =
        createStandardIoosParameter( IoosParameter.peak_wave_period );
    
    @IOOSParameter
    public final Phenomenon PHOSPHORUS = 
        createStandardIoosParameter(IoosParameter.total_phosphorus);

    @IOOSParameter
    public final Phenomenon PHOSPHATE = createStandardIoosParameter( IoosParameter.phosphate );
    
    @HomelessParameter(description="Solar radiation from 400 to 700 nanometers",source="HADS")
    public final Phenomenon PHOTOSYNTHETICALLY_ACTIVE_RADIATION = createPhenomenon(
         "Photosynthetically Active Radiation"
        ,GENERIC_FAKE_MMI_URL_PREFIX + "photosynthetically_active_radiation"
        ,CustomUnits.instance().WATTS_PER_SQUARE_METER
    );
    
    @HomelessParameter(description="",source="")
    public final Phenomenon PHYCOERYTHRIN = createPhenomenon(
         "Phycoerythrin"
        ,GENERIC_FAKE_MMI_URL_PREFIX + "phycoerythrin"
        ,"rfu"
    );

    @IOOSParameter
    public final Phenomenon PRECIPITATION_ACCUMULATED =
        createStandardIoosParameter( IoosParameter.precipitation_accumulated );

    @HomelessParameter(description="",source="HADS, NERRS, SnoTel")
    public final Phenomenon PRECIPITATION_INCREMENT = createPhenomenon(
        "Precipitation Increment"
       ,GENERIC_FAKE_MMI_URL_PREFIX + "precipitation_increment"
       ,SI.METER
    );

    @HomelessParameter(description="",source="")
    public final Phenomenon WIND_CHILL_TEMPERATURE = createPhenomenon(
            "wind chill temperature"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "wind_chill_temperature"
            ,SI.DEGREE_CELSIUS
    );

    @HomelessParameter(description="",source="Alaska Department of Fish and Game")
    public final Phenomenon ABUNDANCE_OF_ONCORHYNCHUS_MYKISS = createPhenomenon(
            "abundance of oncorhynchus mykiss"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "abundance_of_oncorhynchus_mykiss"
            ,"individuals"
    );

    @HomelessParameter(description="",source="Alaska Department of Fish and Game")
    public final Phenomenon ABUNDANCE_OF_ONCORHYNCHUS_NERKA = createPhenomenon(
            "abundance of oncorhynchus nerka"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "abundance_of_oncorhynchus_nerka"
            ,"individuals"
    );

    @HomelessParameter(description="",source="Alaska Department of Fish and Game")
    public final Phenomenon ABUNDANCE_OF_ONCORHYNCHUS_GORBUSCHA = createPhenomenon(
            "abundance of oncorhynchus gorbuscha"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "abundance_of_oncorhynchus_gorbuscha"
            ,"individuals"
    );

    @HomelessParameter(description="",source="Alaska Department of Fish and Game")
    public final Phenomenon ABUNDANCE_OF_ONCORHYNCHUS_KETA = createPhenomenon(
            "abundance of oncorhynchus keta"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "abundance_of_oncorhynchus_keta"
            ,"individuals"
    );

    @HomelessParameter(description="",source="Alaska Department of Fish and Game")
    public final Phenomenon ABUNDANCE_OF_ONCORHYNCHUS_TSHAWYTSCHA = createPhenomenon(
            "abundance of oncorhynchus tshawytscha"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "abundance_of_oncorhynchus_tshawytscha"
            ,"individuals"
    );

    @HomelessParameter(description="",source="Alaska Department of Fish and Game")
    public final Phenomenon ABUNDANCE_OF_ONCORHYNCHUS_KISUTCH = createPhenomenon(
            "abundance of oncorhynchus kisutch"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "abundance_of_oncorhynchus_kisutch"
            ,"individuals"
    );

    @HomelessParameter(description="",source="Alaska Department of Fish and Game")
    public final Phenomenon ABUNDANCE_OF_SALVELINUS_MALMA = createPhenomenon(
            "aabundance of salvelinus malma"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "abundance_of_salvelinus_malma"
            ,"individuals"
    );

    @HomelessParameter(description="",source="Alaska Department of Fish and Game")
    public final Phenomenon ABUNDANCE_OF_FISH_SPECIES_UNKNOWN = createPhenomenon(
            "abundance of fish species unknown"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "abundance_of_fish_species_unknown"
            ,"individuals"
    );

    @CFParameter
    public final Phenomenon EASTWARD_SEA_WATER_VELOCITY =
            createStandardCfParameter( CFStandardNames.EASTWARD_SEA_WATER_VELOCITY );


    @HomelessParameter(description="",source="")
    public final Phenomenon VOLUME_BEAM_ATTENUATION_COEFFICIENT_OF_RADIATIVE_FLUX_IN_SEA_WATER = createPhenomenon(
            "volume beam attenuation coefficient of radiative flux in sea water"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "volume_beam_attenuation_coefficient_of_radiative_flux_in_sea_water"
            ,"m-1"
    );

    @CFParameter
    public final Phenomenon PRODUCT_OF_AIR_TEMPERATURE_AND_SPECIFIC_HUMIDITY =
        createStandardCfParameter( CFStandardNames.PRODUCT_OF_AIR_TEMPERATURE_AND_SPECIFIC_HUMIDITY );

    @CFParameter
    public final Phenomenon RADIAL_SEA_WATER_VELOCITY_AWAY_FROM_INSTRUMENT =
        createStandardCfParameter( CFStandardNames.RADIAL_SEA_WATER_VELOCITY_AWAY_FROM_INSTRUMENT );

    @CFParameter
    @NonStandardUnits
    public final Phenomenon RELATIVE_HUMIDITY = createCfParameterWithAlternateUnits(
         CFStandardNames.RELATIVE_HUMIDITY
        ,CustomUnits.instance().PERCENTAGE
    );

    @HomelessParameter(description="",source="NERRS, RAWS, SnoTel")
    public final Phenomenon RELATIVE_HUMIDITY_AVERAGE = createPhenomenon(
        "Relative Humidity Average"
       ,GENERIC_FAKE_MMI_URL_PREFIX + "relative_humidity_average"
       ,CustomUnits.instance().PERCENTAGE
    );
    
    @HomelessParameter(description="",source="RAWS, SnoTel")
    public final Phenomenon RELATIVE_HUMIDITY_MAXIMUM = createPhenomenon(
        "Relative Humidity Maximum"
       ,GENERIC_FAKE_MMI_URL_PREFIX + "relative_humidity_maximum"
       ,CustomUnits.instance().PERCENTAGE       
    );
    
    @HomelessParameter(description="",source="RAWS, SnoTel")
    public final Phenomenon RELATIVE_HUMIDITY_MINIMUM = createPhenomenon(
        "Relative Humidity Minimum"
       ,GENERIC_FAKE_MMI_URL_PREFIX + "relative_humidity_minimum"
       ,CustomUnits.instance().PERCENTAGE       
    );
    
    //aka dielectric constant
    @HomelessParameter(description="",source="SnoTel")
    public final Phenomenon RELATIVE_PERMITTIVITY = createPhenomenon(
         "Relative Permittivity"
        ,GENERIC_FAKE_MMI_URL_PREFIX + "relative_permittivity"
        ,CustomUnits.instance().PERCENTAGE        
    );

    @IOOSParameter    
    public final Phenomenon RIVER_DISCHARGE = createIoosParameterWithAlternateUnits(
         IoosParameter.river_discharge
        ,CustomUnits.instance().CUBIC_METERS_PER_SECOND
    );

    @CFParameter
    public final Phenomenon SEA_FLOOR_DEPTH_BELOW_SEA_SURFACE =
        createStandardCfParameter( CFStandardNames.SEA_FLOOR_DEPTH_BELOW_SEA_SURFACE );

    @CFParameter
    public final Phenomenon SEA_SURFACE_HEIGHT_ABOVE_SEA_LEVEL =
        createStandardCfParameter( CFStandardNames.SEA_SURFACE_HEIGHT_ABOVE_SEA_LEVEL );
    
    @CFParameter
    public final Phenomenon SEA_SURFACE_HEIGHT_AMPLITUDE_DUE_TO_GEOCENTRIC_OCEAN_TIDE = 
            createStandardCfParameter( CFStandardNames.SEA_SURFACE_HEIGHT_AMPLITUDE_DUE_TO_GEOCENTRIC_OCEAN_TIDE );
    
    @HomelessParameter(description="",source="")
    public final Phenomenon SEA_SURFACE_MAXIMUM_WAVE_HEIGHT = createPhenomenon(
         "Sea Surface Maximum Wave Height"
        ,GENERIC_FAKE_MMI_URL_PREFIX + "sea_surface_maximum_wave_height"
        ,SI.METER
    ); 
    
    @CFParameter
    public final Phenomenon SEA_SURFACE_SWELL_WAVE_PERIOD =
        createStandardCfParameter( CFStandardNames.SEA_SURFACE_SWELL_WAVE_PERIOD );

    @CFParameter
    public final Phenomenon SEA_SURFACE_SWELL_WAVE_SIGNIFICANT_HEIGHT =
        createStandardCfParameter( CFStandardNames.SEA_SURFACE_SWELL_WAVE_SIGNIFICANT_HEIGHT );
    
    @CFParameter
    public final Phenomenon SEA_SURFACE_SWELL_WAVE_TO_DIRECTION =
        createStandardCfParameter( CFStandardNames.SEA_SURFACE_SWELL_WAVE_TO_DIRECTION );
    
	@HomelessParameter(description = "", source = "NOAA NOS CO-OPS")
	public final Phenomenon SEA_SURFACE_DOMINANT_WAVE_TO_DIRECTION = createPhenomenon(
		"Sea Surface Dominant Wave To Direction"
		,GENERIC_FAKE_MMI_URL_PREFIX + "sea_surface_dominant_wave_to_direction"
		,SI.ARC_DEGREE
	);
    
    @CFParameter
    public final Phenomenon SEA_SURFACE_WAVE_FROM_DIRECTION =
        createStandardCfParameter( CFStandardNames.SEA_SURFACE_WAVE_FROM_DIRECTION );
    
    @CFParameter
    public final Phenomenon SEA_SURFACE_WAVE_TO_DIRECTION =
        createStandardCfParameter( CFStandardNames.SEA_SURFACE_WAVE_TO_DIRECTION );
    
    @HomelessParameter(description="",source="NOAA NOS CO-OPS")
    public final Phenomenon SEA_SURFACE_WAVE_MEAN_HEIGHT = createPhenomenon(
         "Sea Surface Wave Mean Height"
        ,GENERIC_FAKE_MMI_URL_PREFIX + "sea_surface_wave_mean_height"
        ,SI.METER
    );
    
    @HomelessParameter(description="",source="")
    public final Phenomenon SEA_SURFACE_WAVE_MEAN_PERIOD = createPhenomenon(
        "Sea Surface Wave Mean Period"
        ,GENERIC_FAKE_MMI_URL_PREFIX + "sea_surface_wave_mean_period"
        ,SI.SECOND
    );
    
    @CFParameter
    public final Phenomenon SEA_SURFACE_WAVE_SIGNIFICANT_HEIGHT =
        createStandardCfParameter( CFStandardNames.SEA_SURFACE_WAVE_SIGNIFICANT_HEIGHT );
    
    @CFParameter
    public final Phenomenon SEA_SURFACE_WIND_WAVE_PERIOD =
        createStandardCfParameter( CFStandardNames.SEA_SURFACE_WIND_WAVE_PERIOD );
        
    @CFParameter
    public final Phenomenon SEA_SURFACE_WIND_WAVE_SIGNIFICANT_HEIGHT =
        createStandardCfParameter( CFStandardNames.SEA_SURFACE_WIND_WAVE_SIGNIFICANT_HEIGHT );

    @CFParameter
    public final Phenomenon SEA_SURFACE_WIND_WAVE_TO_DIRECTION =
        createStandardCfParameter( CFStandardNames.SEA_SURFACE_WIND_WAVE_TO_DIRECTION );

    @CFParameter
    public final Phenomenon SEA_WATER_ELECTRICAL_CONDUCTIVITY =
        createStandardCfParameter( CFStandardNames.SEA_WATER_ELECTRICAL_CONDUCTIVITY );
    
    @CFParameter
    public final Phenomenon SEA_WATER_PH_REPORTED_ON_TOTAL_SCALE = createCfParameterWithAlternateName(
         CFStandardNames.SEA_WATER_PH_REPORTED_ON_TOTAL_SCALE
        ,"Sea Water Acidity"
    );
    
    @CFParameter
    public final Phenomenon SEA_WATER_PRACTICAL_SALINITY = createStandardCfParameter(
         CFStandardNames.SEA_WATER_PRACTICAL_SALINITY
    );
    
    @CFParameter
    public final Phenomenon SEA_WATER_PRESSURE = createCfParameterWithAlternateUnits(
    		CFStandardNames.SEA_WATER_PRESSURE, "decibars");
    
    
    @CFParameter
    public final Phenomenon SEA_WATER_SPEED =
        createStandardCfParameter( CFStandardNames.SEA_WATER_SPEED );

    @CFParameter
    public final Phenomenon SURFACE_SNOW_THICKNESS = createStandardCfParameter( 
    		CFStandardNames.SURFACE_SNOW_THICKNESS);
    
    @CFParameter
    @NonStandardUnits
    public final Phenomenon SEA_WATER_TEMPERATURE = createCfParameterWithAlternateUnits(
         CFStandardNames.SEA_WATER_TEMPERATURE
        ,SI.DEGREE_CELSIUS
    );

    @HomelessParameter(description="",source="")
    public final Phenomenon FREEZING_POINT = createPhenomenon(
            "Freezing Point"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "freezing_point"
            ,SI.DEGREE_CELSIUS
    );

    @HomelessParameter(description="",source="")
    public final Phenomenon DIFFERENCE_BETWEEN_SEA_WATER_TEMPERATURE_AND_FREEZING_POINT = createPhenomenon(
            "difference_between_sea_water_temperature_and_freezing_point"
            ,GENERIC_FAKE_MMI_URL_PREFIX + "difference_between_sea_water_temperature_and_freezing_point"
            ,"degree_Celsius_difference"
    );

    @HomelessParameter(description="",source="HADS, RAWS, SnoTel")
    public final Phenomenon SNOW_DEPTH = createPhenomenon(
        "Snow Depth"
       ,GENERIC_FAKE_MMI_URL_PREFIX + "snow_depth"
       ,SI.METER
    );

    @HomelessParameter(description="",source="RAWS")
    public final Phenomenon SNOW_PILLOW = createPhenomenon(
        "Snow Pillow"
       ,GENERIC_FAKE_MMI_URL_PREFIX + "snow_pillow"
       ,SI.METER
    );
    
    @HomelessParameter(description="",source="HADS, SnoTel")
    public final Phenomenon SNOW_WATER_EQUIVALENT = createPhenomenon(
        "Snow Water Equivalent"
       ,GENERIC_FAKE_MMI_URL_PREFIX + "snow_water_equivalent"
       ,SI.METER
    );

    @HomelessParameter(description="",source="RAWS, SnoTel")
    public final Phenomenon SOIL_MOISTURE_PERCENT = createPhenomenon(
         "Soil Moisture Percent"
        ,GENERIC_FAKE_MMI_URL_PREFIX + "soil_moisture_percent"
        ,CustomUnits.instance().PERCENTAGE
    );

    @CFParameter
    @NonStandardUnits
    public final Phenomenon SOIL_TEMPERATURE = createCfParameterWithAlternateUnits(
         CFStandardNames.SOIL_TEMPERATURE
        ,SI.DEGREE_CELSIUS
    );
    
    @HomelessParameter(description="",source="HADS, NERRS, RAWS, SnoTel")
    public final Phenomenon SOLAR_RADIATION = createPhenomenon(
        "Solar Radiation"
       ,GENERIC_FAKE_MMI_URL_PREFIX + "solar_radiation"
       ,CustomUnits.instance().WATTS_PER_SQUARE_METER
    );
    
    @HomelessParameter(description="",source="SnoTel")
    public final Phenomenon SOLAR_RADIATION_AVERAGE = createPhenomenon(
        "Solar Radiation Average"
       ,GENERIC_FAKE_MMI_URL_PREFIX + "solar_radiation_average"
       ,CustomUnits.instance().WATTS_PER_SQUARE_METER
    );
    
    @HomelessParameter(description="",source="SnoTel")
    public final Phenomenon SOLAR_RADIATION_MAXIMUM = createPhenomenon(
        "Solar Radiation Maximum"
       ,GENERIC_FAKE_MMI_URL_PREFIX + "solar_radiation_maximum"
       ,CustomUnits.instance().WATTS_PER_SQUARE_METER
    );
    
    @HomelessParameter(description="",source="SnoTel")
    public final Phenomenon SOLAR_RADIATION_MINIMUM = createPhenomenon(
        "Solar Radiation Maximum"
       ,GENERIC_FAKE_MMI_URL_PREFIX + "solar_radiation_minimum"
       ,CustomUnits.instance().WATTS_PER_SQUARE_METER
    );
    
    @CFParameter
    public final Phenomenon TOA_INCOMING_SHORTWAVE_FLUX =
        createStandardCfParameter( CFStandardNames.TOA_INCOMING_SHORTWAVE_FLUX );
    
    @CFParameter
    public final Phenomenon TOA_OUTGOING_SHORTWAVE_FLUX =
        createStandardCfParameter( CFStandardNames.TOA_OUTGOING_SHORTWAVE_FLUX );

    @IOOSParameter
    public final Phenomenon TURBIDITY =
        createStandardIoosParameter( IoosParameter.turbidity );

    @CFParameter
    public final Phenomenon VISIBILITY_IN_AIR =
        createStandardCfParameter( CFStandardNames.VISIBILITY_IN_AIR );

    @CFParameter
    public final Phenomenon WATER_SURFACE_HEIGHT_ABOVE_REFERENCE_DATUM =
        createStandardCfParameter( CFStandardNames.WATER_SURFACE_HEIGHT_ABOVE_REFERENCE_DATUM );

    @HomelessParameter(description="",source="USGS")
    public final Phenomenon WATER_TEMPERATURE_INTRAGRAVEL = createPhenomenon(
        "Water Temperature Intragravel"
       ,GENERIC_FAKE_MMI_URL_PREFIX + "water_temperature_intragravel"
       ,SI.DEGREE_CELSIUS
    );
    
    @CFParameter
    public final Phenomenon WATER_VOLUME_TRANSPORT_INTO_SEA_WATER_FROM_RIVERS =
        createStandardCfParameter( CFStandardNames.WATER_VOLUME_TRANSPORT_INTO_SEA_WATER_FROM_RIVERS );
    
    @IOOSParameter
    public final Phenomenon WAVE_DIRECTIONAL_SPREAD =
        createStandardIoosParameter( IoosParameter.wave_directional_spread );
    
    //Used in AOOS
    @HomelessParameter(description="",source="")
    public final Phenomenon WEBCAM = createPhenomenon(
        "Webcam"
        ,GENERIC_FAKE_MMI_URL_PREFIX + "webcam"
    );
    
    @CFParameter
    public final Phenomenon WIND_FROM_DIRECTION =
        createStandardCfParameter( CFStandardNames.WIND_FROM_DIRECTION );
    
    //Used in AOOS
    @HomelessParameter(description="",source="")
    public final Phenomenon WIND_GENERATOR_CURRENT = createPhenomenon(
         "Wind Generator Current"
        ,GENERIC_FAKE_MMI_URL_PREFIX + "wind_generator_current"
        ,CustomUnits.instance().AMPERES_PER_HOUR
    );

    @IOOSParameter
    public final Phenomenon WIND_GUST_FROM_DIRECTION =
        createStandardIoosParameter( IoosParameter.wind_gust_from_direction );

    @CFParameter
    public final Phenomenon WIND_SPEED =
        createStandardCfParameter( CFStandardNames.WIND_SPEED );
    
    @CFParameter
    public final Phenomenon WIND_SPEED_OF_GUST =
        createStandardCfParameter( CFStandardNames.WIND_SPEED_OF_GUST );
    
    @HomelessParameter(description="",source="NOAA NOS CO-OPS")
    public final Phenomenon WIND_VERTICAL_VELOCITY = createPhenomenon(
        "Wind Vertical Velocity"
       ,GENERIC_FAKE_MMI_URL_PREFIX + "wind_vertical_velocity"
       ,CustomUnits.instance().METERS_PER_SECOND
    );
    
    /*
    * Phenomenon from sources that will never map to IOOS sources need a public interface for
    * creating a Parameter.
    */

    public final Phenomenon createHomelessParameter(String name, String units) {
        try {
            return createPhenomenon(name, GENERIC_FAKE_MMI_URL_PREFIX + name, units);
        } catch (Exception ex) {
            try {
                return createPhenomenon(name, GENERIC_FAKE_MMI_URL_PREFIX + name, "");
            } catch (UnitCreationException ex1) {
                return null;
            }
        }
    }

    public final Phenomenon createHomelessParameter(String name, Unit units) {
        try {
            return createPhenomenon(name, GENERIC_FAKE_MMI_URL_PREFIX + name, units);
        } catch (Exception ex) {
            return null;
        }
    }
    
    public final Phenomenon createHomelessParameter(String name, String url, String units) {
        try {
            return createPhenomenon(name, url + name, units);
        } catch (Exception ex) {
            try {
                return createPhenomenon(name, url + name, "");
            } catch (UnitCreationException ex1) {
                return null;
            }
        }
    }

    public final Phenomenon createHomelessParameter(String name, String url, Unit units) {
        try {
            return createPhenomenon(name, url + name, units);
        } catch (Exception ex) {
            return null;
        }
    }

    /*
     * Allows for the phenomenon stored in the SOS DB to be loaded into the phenomena list
     */
    public void loadPhenomenonFromList(java.util.List<Phenomenon> listToLoad) {
        for (Phenomenon phenom : listToLoad) {
            // check to make sure this isn't already in the phenomenon list
            if (!allPhenomena.contains(phenom))
                allPhenomena.add(phenom);
        }
    }
    
    /*
     * Other parameters not previously added
     * @author Sean Cowan
     */
    @IOOSParameter
    public final Phenomenon DISSOLVED_OXYGEN =
        createStandardIoosParameter( IoosParameter.dissolved_oxygen );
    
    @IOOSParameter
    public final Phenomenon DISSOLVED_OXYGEN_SATURATION =
        createStandardIoosParameter( IoosParameter.dissolved_oxygen_saturation );
    
    @IOOSParameter
    public final Phenomenon SALINITY =
        createStandardIoosParameter( IoosParameter.salinity );
    
    @IOOSParameter
    public final Phenomenon ALKALINITY =
    		createIoosParameterWithAlternateUnits( IoosParameter.total_alkalinity, 
    				CustomUnits.instance().MICROMOL_PER_KILOGRAM);
    
    @IOOSParameter
    public final Phenomenon CURRENT_DIRECTION =
        createStandardIoosParameter( IoosParameter.current_to_direction );
    
    @IOOSParameter
    public final Phenomenon CURRENT_SPEED =
        createStandardIoosParameter( IoosParameter.current_speed );
    
    @IOOSParameter
    public final Phenomenon CURRENT_VELOCITY =
        createStandardIoosParameter( IoosParameter.current_velocity );
    
    @IOOSParameter
    public final Phenomenon CHLOROPHYLL =
        createStandardIoosParameter( IoosParameter.chlorophyll_a );
    
    @IOOSParameter
    public final Phenomenon SIGNIFICANT_WAVE_HEIGHT = 
        createStandardIoosParameter( IoosParameter.significant_wave_height );
    
    @IOOSParameter
    public final Phenomenon MEAN_WAVE_PERIOD = 
        createStandardIoosParameter( IoosParameter.mean_wave_period );
    
    // below will not work, i think due to the units string being ambiguous
//    @IOOSParameter
//    public final Phenomenon SPECIFIC_CONDUCTANCE =
//        createStandardIoosParameter( IoosParameter.conductivity );
}